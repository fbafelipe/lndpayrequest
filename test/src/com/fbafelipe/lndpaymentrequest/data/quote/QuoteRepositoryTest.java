package com.fbafelipe.lndpaymentrequest.data.quote;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.fbafelipe.lndpayrequest.data.Clock;
import com.fbafelipe.lndpayrequest.data.Database;
import com.fbafelipe.lndpayrequest.data.quote.QuoteDataSource;
import com.fbafelipe.lndpayrequest.data.quote.QuoteDataSourceFactory;
import com.fbafelipe.lndpayrequest.data.quote.QuoteRepository;
import com.fbafelipe.lndpayrequest.domain.model.Currency;
import com.fbafelipe.lndpayrequest.domain.model.Quote;

public class QuoteRepositoryTest {
	private static final double DELTA = 0.0000001;
	
	private static final long TIME_NOW = 1000000000;
	private static final long TIME_PAST_2_MINS = TIME_NOW - TimeUnit.MINUTES.toMillis(2);
	private static final long TIME_PAST_6_MINS = TIME_NOW - TimeUnit.MINUTES.toMillis(6);
	private static final long TIME_PAST_2_DAYS = TIME_NOW - TimeUnit.DAYS.toMillis(2);
	
	private QuoteRepository mQuoteRepository;
	
	private QuoteDataSource mBrlQuoteDataSource;
	private QuoteDataSource mUsdQuoteDataSource;
	
	private Database mDatabase;
	private Clock mClock;
	private Map<Currency, Quote> mCache;
	
	@Before
	public void setUp() throws Exception {
		mBrlQuoteDataSource = mock(QuoteDataSource.class);
		mUsdQuoteDataSource = mock(QuoteDataSource.class);
		
		mDatabase = mock(Database.class);
		mClock = mock(Clock.class);
		when(mClock.currentTimeMillis()).thenReturn(TIME_NOW);
		mCache = new HashMap<>();
		
		mQuoteRepository = new QuoteRepository(createQuoteDataSourceFactory(), mDatabase, mClock, mCache);
	}
	
	@Test
	public void testFirstQuote() throws Exception {
		when(mBrlQuoteDataSource.getBitcoinValue()).thenReturn(40000.0);
		when(mUsdQuoteDataSource.getBitcoinValue()).thenReturn(10000.0);
		when(mDatabase.selectQuote(Currency.BRL)).thenReturn(null);
		
		Double value = mQuoteRepository.getBitcoinValue(Currency.BRL);
		
		assertNotNull(value);
		assertEquals(40000.0, value, DELTA);
		verify(mBrlQuoteDataSource).getBitcoinValue();
		verify(mDatabase).selectQuote(Currency.BRL);
		verify(mDatabase).updateQuote(Mockito.any());
		verifyNoMoreInteractions(mDatabase);
		verifyZeroInteractions(mUsdQuoteDataSource);
		assertQuoteEquals(new Quote(Currency.BRL, 40000.0, TIME_NOW),  mCache.get(Currency.BRL));
	}
	
	@Test
	public void testCachedQuote() throws Exception {
		Quote quote = new Quote(Currency.BRL, 40000.0, TIME_PAST_2_MINS);
		mCache.put(Currency.BRL, quote);
		
		Double value = mQuoteRepository.getBitcoinValue(Currency.BRL);
		
		assertNotNull(value);
		assertEquals(40000.0, value, DELTA);
		verifyZeroInteractions(mBrlQuoteDataSource);
		verifyZeroInteractions(mDatabase);
		verifyZeroInteractions(mUsdQuoteDataSource);
		assertQuoteEquals(quote, mCache.get(Currency.BRL));
	}
	
	@Test
	public void testDatabaseQuote() throws Exception {
		Quote databaseQuote = new Quote(Currency.BRL, 38000.0, TIME_PAST_2_MINS);
		when(mDatabase.selectQuote(Currency.BRL)).thenReturn(databaseQuote);
		
		Double value = mQuoteRepository.getBitcoinValue(Currency.BRL);
		
		assertNotNull(value);
		assertEquals(38000.0, value, DELTA);
		verify(mDatabase).selectQuote(Currency.BRL);
		verifyNoMoreInteractions(mDatabase);
		verifyZeroInteractions(mBrlQuoteDataSource);
		verifyZeroInteractions(mUsdQuoteDataSource);
		assertQuoteEquals(databaseQuote, mCache.get(Currency.BRL));
	}
	
	@Test
	public void testCachedExpiredQuote() throws Exception {
		mCache.put(Currency.BRL, new Quote(Currency.BRL, 40000.0, TIME_PAST_6_MINS));
		when(mBrlQuoteDataSource.getBitcoinValue()).thenReturn(50000.0);
		
		Double value = mQuoteRepository.getBitcoinValue(Currency.BRL);
		
		assertNotNull(value);
		assertEquals(50000.0, value, DELTA);
		verify(mBrlQuoteDataSource).getBitcoinValue();
		verify(mDatabase).updateQuote(Mockito.any());
		verifyNoMoreInteractions(mDatabase);
		verifyZeroInteractions(mUsdQuoteDataSource);
		assertQuoteEquals(new Quote(Currency.BRL, 50000.0, TIME_NOW), mCache.get(Currency.BRL));
	}
	
	@Test
	public void testCachedDatabaseExpireQuote() throws Exception {
		Quote databaseQuote = new Quote(Currency.BRL, 38000.0, TIME_PAST_6_MINS);
		when(mDatabase.selectQuote(Currency.BRL)).thenReturn(databaseQuote);
		when(mBrlQuoteDataSource.getBitcoinValue()).thenReturn(40000.0);
		
		Double value = mQuoteRepository.getBitcoinValue(Currency.BRL);
		
		assertNotNull(value);
		assertEquals(40000.0, value, DELTA);
		verify(mBrlQuoteDataSource).getBitcoinValue();
		verify(mDatabase).selectQuote(Currency.BRL);
		verify(mDatabase).updateQuote(Mockito.any());
		verifyNoMoreInteractions(mDatabase);
		verifyZeroInteractions(mUsdQuoteDataSource);
	}
	
	@Test
	public void testReturnOldCacheOnFetchError() throws Exception {
		Quote quote = new Quote(Currency.BRL, 40000.0, TIME_PAST_6_MINS);
		mCache.put(Currency.BRL, quote);
		when(mBrlQuoteDataSource.getBitcoinValue()).thenReturn(null);
		
		Double value = mQuoteRepository.getBitcoinValue(Currency.BRL);
		
		assertNotNull(value);
		assertEquals(40000.0, value, DELTA);
		verify(mBrlQuoteDataSource).getBitcoinValue();
		verifyNoMoreInteractions(mDatabase);
		verifyZeroInteractions(mUsdQuoteDataSource);
		assertQuoteEquals(quote, mCache.get(Currency.BRL));
	}
	
	@Test
	public void testReturnOldDatabaseOnFetchError() throws Exception {
		Quote databaseQuote = new Quote(Currency.BRL, 38000.0, TIME_PAST_6_MINS);
		when(mDatabase.selectQuote(Currency.BRL)).thenReturn(databaseQuote);
		when(mBrlQuoteDataSource.getBitcoinValue()).thenReturn(null);
		
		Double value = mQuoteRepository.getBitcoinValue(Currency.BRL);
		
		assertNotNull(value);
		assertEquals(38000.0, value, DELTA);
		verify(mBrlQuoteDataSource).getBitcoinValue();
		verify(mDatabase).selectQuote(Currency.BRL);
		verifyNoMoreInteractions(mDatabase);
		verifyZeroInteractions(mUsdQuoteDataSource);
	}
	
	@Test
	public void testReturnErrorOnVeryOldCacheAndFetchError() throws Exception {
		Quote quote = new Quote(Currency.BRL, 40000.0, TIME_PAST_2_DAYS);
		mCache.put(Currency.BRL, quote);
		when(mBrlQuoteDataSource.getBitcoinValue()).thenReturn(null);
		
		Double value = mQuoteRepository.getBitcoinValue(Currency.BRL);
		
		assertNull(value);
		verify(mBrlQuoteDataSource).getBitcoinValue();
		verifyNoMoreInteractions(mBrlQuoteDataSource);
		verifyNoMoreInteractions(mDatabase);
		verifyZeroInteractions(mUsdQuoteDataSource);
	}
	
	@Test
	public void testReturnErrorOnVeryOldDatabaseAndFetchError() throws Exception {
		Quote databaseQuote = new Quote(Currency.BRL, 38000.0, TIME_PAST_2_DAYS);
		when(mDatabase.selectQuote(Currency.BRL)).thenReturn(databaseQuote);
		when(mBrlQuoteDataSource.getBitcoinValue()).thenReturn(null);
		
		Double value = mQuoteRepository.getBitcoinValue(Currency.BRL);
		
		assertNull(value);
		verify(mBrlQuoteDataSource).getBitcoinValue();
		verifyNoMoreInteractions(mBrlQuoteDataSource);
		verify(mDatabase).selectQuote(Currency.BRL);
		verifyNoMoreInteractions(mDatabase);
		verifyZeroInteractions(mUsdQuoteDataSource);
	}
	
	private QuoteDataSourceFactory createQuoteDataSourceFactory() {
		Map<Currency, QuoteDataSource> dataSourceMap = new HashMap<>();
		dataSourceMap.put(Currency.BRL, mBrlQuoteDataSource);
		dataSourceMap.put(Currency.USD, mUsdQuoteDataSource);
		
		QuoteDataSourceFactory quoteDataSourceFactory = mock(QuoteDataSourceFactory.class);
		when(quoteDataSourceFactory.createQuoteDataSources()).thenReturn(dataSourceMap);
		
		return quoteDataSourceFactory;
	}
	
	private void assertQuoteEquals(Quote expected, Quote result) {
		assertNotNull(result);
		assertEquals(expected.currency, result.currency);
		assertEquals(expected.bitcoinValue, result.bitcoinValue, DELTA);
		assertEquals(expected.lastUpdate, result.lastUpdate);
	}
}
