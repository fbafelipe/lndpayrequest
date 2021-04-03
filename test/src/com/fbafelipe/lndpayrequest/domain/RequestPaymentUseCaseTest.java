package com.fbafelipe.lndpayrequest.domain;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.fbafelipe.lndpayrequest.data.Clock;
import com.fbafelipe.lndpayrequest.data.Database;
import com.fbafelipe.lndpayrequest.data.LndNode;
import com.fbafelipe.lndpayrequest.data.quote.QuoteRepository;
import com.fbafelipe.lndpayrequest.domain.model.Currency;
import com.fbafelipe.lndpayrequest.domain.model.Invoice;
import com.fbafelipe.lndpayrequest.domain.model.InvoiceStatus;
import com.fbafelipe.lndpayrequest.domain.model.PaymentRequest;
import com.fbafelipe.lndpayrequest.exception.ServerError;
import com.fbafelipe.lndpayrequest.exception.ServerException;

public class RequestPaymentUseCaseTest {
	private static final String APIKEY = "test";
	
	private static final String PAYMENT_ID = "payment_id";
	private static final String PAYMENT_REQUEST = "payment_request";
	private static final long ACCOUNT_ID = 1L;
	
	private RequestPaymentUseCase mRequestPaymentUseCase;
	
	private Database mDatabase;
	private QuoteRepository mQuoteRepository;
	private LndNode mLndNode;
	private Clock mClock;
	
	@Before
	public void setUp() throws Exception {
		mDatabase = mock(Database.class);
		mQuoteRepository = mock(QuoteRepository.class);
		mLndNode = mock(LndNode.class);
		mClock = mock(Clock.class);
		
		when(mClock.currentTimeMillis()).thenReturn(1000000L);
		
		mRequestPaymentUseCase = new RequestPaymentUseCase(mDatabase, mQuoteRepository, mLndNode, mClock);
	}
	
	@Test
	public void testRequestSatoshi() throws Exception {
		when(mLndNode.addInvoice(1000L)).thenAnswer(answerInvoiceWithExpectedValue(1000L));
		when(mDatabase.selectAccountIdFromApikey(APIKEY)).thenReturn(ACCOUNT_ID);
		Mockito.doAnswer(answerInsertInvoiceWithChecks(1000L)).when(mDatabase).insertInvoice(Mockito.any());
		
		PaymentRequest request = mRequestPaymentUseCase.requestPayment(APIKEY, 1000L, Currency.SATOSHI);
		
		assertNotNull(request.paymentId);
		assertNotNull(request.paymentRequest);
		verify(mLndNode).addInvoice(1000L);
		verifyNoMoreInteractions(mLndNode);
		verify(mDatabase).selectAccountIdFromApikey(APIKEY);
		verify(mDatabase).insertInvoice(Mockito.any());
		verifyNoMoreInteractions(mDatabase);
		verifyZeroInteractions(mQuoteRepository);
	}
	
	@Test
	public void testRequestSatoshiFraction() throws Exception {
		when(mLndNode.addInvoice(1000L)).thenAnswer(answerInvoiceWithExpectedValue(1000L));
		when(mDatabase.selectAccountIdFromApikey(APIKEY)).thenReturn(ACCOUNT_ID);
		Mockito.doAnswer(answerInsertInvoiceWithChecks(1000L)).when(mDatabase).insertInvoice(Mockito.any());
		
		PaymentRequest request = mRequestPaymentUseCase.requestPayment(APIKEY, 1000.1, Currency.SATOSHI);
		
		assertNotNull(request.paymentId);
		assertNotNull(request.paymentRequest);
		verify(mLndNode).addInvoice(1000L);
		verifyNoMoreInteractions(mLndNode);
		verify(mDatabase).selectAccountIdFromApikey(APIKEY);
		verify(mDatabase).insertInvoice(Mockito.any());
		verifyNoMoreInteractions(mDatabase);
		verifyZeroInteractions(mQuoteRepository);
	}
	
	@Test
	public void testRequestFiat() throws Exception {
		double bitcoinValue = 36000.0;
		double amountBrl = 1.0;
		long amountSat = 2778;
		
		when(mQuoteRepository.getBitcoinValue(Currency.BRL)).thenReturn(bitcoinValue);
		
		when(mLndNode.addInvoice(amountSat)).thenAnswer(answerInvoiceWithExpectedValue(amountSat));
		when(mDatabase.selectAccountIdFromApikey(APIKEY)).thenReturn(ACCOUNT_ID);
		Mockito.doAnswer(answerInsertInvoiceWithChecks(amountSat)).when(mDatabase).insertInvoice(Mockito.any());
		
		PaymentRequest request = mRequestPaymentUseCase.requestPayment(APIKEY, amountBrl, Currency.BRL);
		
		assertNotNull(request.paymentId);
		assertNotNull(request.paymentRequest);
		verify(mLndNode).addInvoice(amountSat);
		verifyNoMoreInteractions(mLndNode);
		verify(mDatabase).selectAccountIdFromApikey(APIKEY);
		verify(mDatabase).insertInvoice(Mockito.any());
		verifyNoMoreInteractions(mDatabase);
		verify(mQuoteRepository).getBitcoinValue(Currency.BRL);
		verifyNoMoreInteractions(mQuoteRepository);
	}
	
	@Test
	public void testRequestFiatLong() throws Exception {
		double bitcoinValue = 36000.0;
		long amountBrl = 1;
		long amountSat = 2778;
		
		when(mQuoteRepository.getBitcoinValue(Currency.BRL)).thenReturn(bitcoinValue);
		
		when(mLndNode.addInvoice(amountSat)).thenAnswer(answerInvoiceWithExpectedValue(amountSat));
		when(mDatabase.selectAccountIdFromApikey(APIKEY)).thenReturn(ACCOUNT_ID);
		Mockito.doAnswer(answerInsertInvoiceWithChecks(amountSat)).when(mDatabase).insertInvoice(Mockito.any());
		
		PaymentRequest request = mRequestPaymentUseCase.requestPayment(APIKEY, amountBrl, Currency.BRL);
		
		assertNotNull(request.paymentId);
		assertNotNull(request.paymentRequest);
		verify(mLndNode).addInvoice(amountSat);
		verifyNoMoreInteractions(mLndNode);
		verify(mDatabase).selectAccountIdFromApikey(APIKEY);
		verify(mDatabase).insertInvoice(Mockito.any());
		verifyNoMoreInteractions(mDatabase);
		verify(mQuoteRepository).getBitcoinValue(Currency.BRL);
		verifyNoMoreInteractions(mQuoteRepository);
	}
	
	@Test
	public void testInvalidApiKey() throws Exception {
		when(mDatabase.selectAccountIdFromApikey(APIKEY)).thenReturn(null);
		
		try {
			mRequestPaymentUseCase.requestPayment(APIKEY, 1000L, Currency.SATOSHI);
			fail("Should have thrown");
		}
		catch (ServerException e) {
			assertEquals(ServerError.BAD_REQUEST, e.getError());
		}
	}
	
	@Test
	public void testInvalidNegativeAmount() throws Exception {
		when(mDatabase.selectAccountIdFromApikey(APIKEY)).thenReturn(ACCOUNT_ID);
		
		try {
			mRequestPaymentUseCase.requestPayment(APIKEY, -1L, Currency.SATOSHI);
			fail("Should have thrown");
		}
		catch (ServerException e) {
			assertEquals(ServerError.BAD_REQUEST, e.getError());
		}
	}
	
	@Test
	public void testInvalidZeroAmount() throws Exception {
		when(mDatabase.selectAccountIdFromApikey(APIKEY)).thenReturn(ACCOUNT_ID);
		
		try {
			mRequestPaymentUseCase.requestPayment(APIKEY, 0L, Currency.SATOSHI);
			fail("Should have thrown");
		}
		catch (ServerException e) {
			assertEquals(ServerError.BAD_REQUEST, e.getError());
		}
	}
	
	@Test
	public void testInvalidAmountTooLarge() throws Exception {
		when(mDatabase.selectAccountIdFromApikey(APIKEY)).thenReturn(ACCOUNT_ID);
		
		try {
			mRequestPaymentUseCase.requestPayment(APIKEY, 10000001L, Currency.SATOSHI);
			fail("Should have thrown");
		}
		catch (ServerException e) {
			assertEquals(ServerError.BAD_REQUEST, e.getError());
		}
	}
	
	@Test
	public void testInvalidPositiveInfiniteSatoshiAmount() throws Exception {
		when(mDatabase.selectAccountIdFromApikey(APIKEY)).thenReturn(ACCOUNT_ID);
		
		try {
			mRequestPaymentUseCase.requestPayment(APIKEY, Double.POSITIVE_INFINITY, Currency.SATOSHI);
			fail("Should have thrown");
		}
		catch (ServerException e) {
			assertEquals(ServerError.BAD_REQUEST, e.getError());
		}
	}
	
	@Test
	public void testInvalidNegativeInfiniteSatoshiAmount() throws Exception {
		when(mDatabase.selectAccountIdFromApikey(APIKEY)).thenReturn(ACCOUNT_ID);
		
		try {
			mRequestPaymentUseCase.requestPayment(APIKEY, Double.NEGATIVE_INFINITY, Currency.SATOSHI);
			fail("Should have thrown");
		}
		catch (ServerException e) {
			assertEquals(ServerError.BAD_REQUEST, e.getError());
		}
	}
	
	@Test
	public void testInvalidNaNSatoshiAmount() throws Exception {
		when(mDatabase.selectAccountIdFromApikey(APIKEY)).thenReturn(ACCOUNT_ID);
		
		try {
			mRequestPaymentUseCase.requestPayment(APIKEY, Double.NaN, Currency.SATOSHI);
			fail("Should have thrown");
		}
		catch (ServerException e) {
			assertEquals(ServerError.BAD_REQUEST, e.getError());
		}
	}
	
	@Test
	public void testInvalidPositiveInfiniteBrlAmount() throws Exception {
		when(mDatabase.selectAccountIdFromApikey(APIKEY)).thenReturn(ACCOUNT_ID);
		when(mQuoteRepository.getBitcoinValue(Currency.BRL)).thenReturn(36000.0);
		
		try {
			mRequestPaymentUseCase.requestPayment(APIKEY, Double.POSITIVE_INFINITY, Currency.BRL);
			fail("Should have thrown");
		}
		catch (ServerException e) {
			assertEquals(ServerError.BAD_REQUEST, e.getError());
		}
	}
	
	@Test
	public void testInvalidNegativeInfiniteBrlAmount() throws Exception {
		when(mDatabase.selectAccountIdFromApikey(APIKEY)).thenReturn(ACCOUNT_ID);
		when(mQuoteRepository.getBitcoinValue(Currency.BRL)).thenReturn(36000.0);
		
		try {
			mRequestPaymentUseCase.requestPayment(APIKEY, Double.NEGATIVE_INFINITY, Currency.BRL);
			fail("Should have thrown");
		}
		catch (ServerException e) {
			assertEquals(ServerError.BAD_REQUEST, e.getError());
		}
	}
	
	@Test
	public void testInvalidNaNBrlAmount() throws Exception {
		when(mDatabase.selectAccountIdFromApikey(APIKEY)).thenReturn(ACCOUNT_ID);
		when(mQuoteRepository.getBitcoinValue(Currency.BRL)).thenReturn(36000.0);
		
		try {
			mRequestPaymentUseCase.requestPayment(APIKEY, Double.NaN, Currency.BRL);
			fail("Should have thrown");
		}
		catch (ServerException e) {
			assertEquals(ServerError.BAD_REQUEST, e.getError());
		}
	}
	
	private Answer<Void> answerInsertInvoiceWithChecks(long expectedAmount) {
		return new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Invoice invoice = invocation.getArgument(0);
				assertEquals(ACCOUNT_ID, invoice.accountId);
				assertEquals(expectedAmount, invoice.amountSat);
				assertEquals(InvoiceStatus.OPEN, invoice.status);
				return null;
			}
			
		};
	}
	
	private Answer<Invoice> answerInvoiceWithExpectedValue(long expectedAmount) {
		return new Answer<Invoice>() {
			@Override
			public Invoice answer(InvocationOnMock invocation) throws Throwable {
				long amount = invocation.getArgument(0);
				assertEquals(expectedAmount, amount);
				
				Invoice invoice = new Invoice();
				invoice.paymentId = PAYMENT_ID;
				invoice.paymentRequest = PAYMENT_REQUEST;
				invoice.amountSat = expectedAmount;
				return invoice;
			}
			
		};
	}
}
