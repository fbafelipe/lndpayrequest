package com.fbafelipe.lndpayrequest.data.quote;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fbafelipe.lndpayrequest.data.Clock;
import com.fbafelipe.lndpayrequest.data.Database;
import com.fbafelipe.lndpayrequest.domain.model.Currency;
import com.fbafelipe.lndpayrequest.domain.model.Quote;

public class QuoteRepository {
	private static final Logger LOGGER = Logger.getLogger(QuoteRepository.class.getSimpleName());
	
	public static long RENEW_CACHE_TIME = TimeUnit.MINUTES.toMillis(5);
	public static long MAX_CACHE_TIME = TimeUnit.DAYS.toMillis(1);
	
	private Database mDatabase;
	private Clock mClock;
	
	private Map<Currency, QuoteDataSource> mDataSources;
	
	private Map<Currency, Quote> mCache = new HashMap<>();
	
	public QuoteRepository(QuoteDataSourceFactory dataSourceFactory, Database database, Clock clock) {
		this(dataSourceFactory, database, clock, new HashMap<>());
	}
	
	public QuoteRepository(QuoteDataSourceFactory dataSourceFactory, Database database, Clock clock, Map<Currency, Quote> cache) {
		mDataSources = dataSourceFactory.createQuoteDataSources();
		mDatabase = database;
		mClock = clock;
		mCache = cache;
	}
	
	public Double getBitcoinValue(Currency currency) {
		Quote cachedQuote = getCachedQuote(currency);
		
		if (cachedQuote != null && timestampNotOlderThan(cachedQuote.lastUpdate, RENEW_CACHE_TIME))
			return cachedQuote.bitcoinValue;
		
		Double fetchedQuote = fetchQuote(currency);
		if (fetchedQuote != null) {
			updateQuote(currency, fetchedQuote);
			return fetchedQuote;
		}
		else if (cachedQuote != null && timestampNotOlderThan(cachedQuote.lastUpdate, MAX_CACHE_TIME)) {
			return cachedQuote.bitcoinValue;
		}
		
		return null;
	}
	
	private void updateQuote(Currency currency, double bitcoinValue) {
		Quote quote = new Quote();
		quote.currency = currency;
		quote.bitcoinValue = bitcoinValue;
		quote.lastUpdate = mClock.currentTimeMillis();
		
		mCache.put(currency, quote);
		
		try {
			mDatabase.updateQuote(quote);
		}
		catch (SQLException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	private Quote getCachedQuote(Currency currency) {
		Quote quote = mCache.get(currency);
		
		if (quote == null) {
			try {
				quote = mDatabase.selectQuote(currency);
				mCache.put(currency, quote);
			}
			catch (SQLException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		
		return quote;
	}
	
	private Double fetchQuote(Currency currency) {
		QuoteDataSource dataSource = mDataSources.get(currency);
		return dataSource != null ? dataSource.getBitcoinValue() : null;
	}
	
	private boolean timestampNotOlderThan(long timestamp, long time) {
		return timestamp > mClock.currentTimeMillis() - time;
	}
}
