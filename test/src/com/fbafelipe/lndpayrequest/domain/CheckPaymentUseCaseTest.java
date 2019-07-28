package com.fbafelipe.lndpayrequest.domain;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.fbafelipe.lndpayrequest.data.Database;
import com.fbafelipe.lndpayrequest.data.LndNode;
import com.fbafelipe.lndpayrequest.domain.model.Invoice;
import com.fbafelipe.lndpayrequest.domain.model.InvoiceStatus;
import com.fbafelipe.lndpayrequest.domain.model.LookupInvoice;
import com.fbafelipe.lndpayrequest.exception.ServerException;

public class CheckPaymentUseCaseTest {
	private static final String PAYMENT_ID = "test";
	
	private CheckPaymentUseCase mCheckPaymentUseCase;
	
	private Database mDatabase;
	private LndNode mLndNode;
	
	@Before
	public void setUp() throws Exception {
		mDatabase = mock(Database.class);
		mLndNode = mock(LndNode.class);
		
		mCheckPaymentUseCase = new CheckPaymentUseCase(mDatabase, mLndNode);
	}
	
	@Test
	public void testPaymentIsPaidOnDatabase() throws Exception {
		Invoice invoice = new Invoice();
		invoice.paymentId = PAYMENT_ID;
		invoice.amountSat = 1000;
		invoice.paid = true;
		when(mDatabase.selectInvoice(PAYMENT_ID)).thenReturn(invoice);
		
		boolean result = mCheckPaymentUseCase.isPaymentDone(PAYMENT_ID);
		
		assertTrue(result);
		verify(mDatabase).selectInvoice(PAYMENT_ID);
		verifyNoMoreInteractions(mDatabase);
		verifyZeroInteractions(mLndNode);
	}
	
	@Test
	public void testPaymentIsPaidOnLndNode() throws Exception {
		Invoice invoice = new Invoice();
		invoice.paymentId = PAYMENT_ID;
		invoice.amountSat = 1000;
		invoice.paid = false;
		when(mDatabase.selectInvoice(PAYMENT_ID)).thenReturn(invoice);
		
		LookupInvoice lookupInvoice = new LookupInvoice();
		lookupInvoice.amountPaidSat = 1000L;
		lookupInvoice.status = InvoiceStatus.SETTLED;
		when(mLndNode.lookupInvoice(invoice)).thenReturn(lookupInvoice);
		
		boolean result = mCheckPaymentUseCase.isPaymentDone(PAYMENT_ID);
		
		assertTrue(result);
		verify(mDatabase).selectInvoice(PAYMENT_ID);
		verify(mDatabase).updatePaidInvoice(Mockito.any());
		verifyNoMoreInteractions(mDatabase);
		verify(mLndNode).lookupInvoice(Mockito.any());
		verifyNoMoreInteractions(mLndNode);
	}
	
	@Test
	public void testPaymentIsNotPaidLookupInvoiceIsZero() throws Exception {
		Invoice invoice = new Invoice();
		invoice.paymentId = PAYMENT_ID;
		invoice.amountSat = 1000;
		invoice.paid = false;
		when(mDatabase.selectInvoice(PAYMENT_ID)).thenReturn(invoice);
		
		LookupInvoice lookupInvoice = new LookupInvoice();
		lookupInvoice.amountPaidSat = 0L;
		lookupInvoice.status = InvoiceStatus.OPEN;
		when(mLndNode.lookupInvoice(invoice)).thenReturn(lookupInvoice);
		
		boolean result = mCheckPaymentUseCase.isPaymentDone(PAYMENT_ID);
		
		assertFalse(result);
		verify(mDatabase).selectInvoice(PAYMENT_ID);
		verifyNoMoreInteractions(mDatabase);
		verify(mLndNode).lookupInvoice(Mockito.any());
		verifyNoMoreInteractions(mLndNode);
	}
	
	@Test
	public void testPaymentIsNotPaidLookupInvoiceIsNull() throws Exception {
		Invoice invoice = new Invoice();
		invoice.paymentId = PAYMENT_ID;
		invoice.amountSat = 1000;
		invoice.paid = false;
		when(mDatabase.selectInvoice(PAYMENT_ID)).thenReturn(invoice);
		
		LookupInvoice lookupInvoice = new LookupInvoice();
		lookupInvoice.amountPaidSat = null;
		lookupInvoice.status = InvoiceStatus.OPEN;
		when(mLndNode.lookupInvoice(invoice)).thenReturn(lookupInvoice);
		
		boolean result = mCheckPaymentUseCase.isPaymentDone(PAYMENT_ID);
		
		assertFalse(result);
		verify(mDatabase).selectInvoice(PAYMENT_ID);
		verifyNoMoreInteractions(mDatabase);
		verify(mLndNode).lookupInvoice(Mockito.any());
		verifyNoMoreInteractions(mLndNode);
	}
	
	@Test
	public void testPaymentIsNotPaidLookupInvoiceValueIsSmaller() throws Exception {
		Invoice invoice = new Invoice();
		invoice.paymentId = PAYMENT_ID;
		invoice.amountSat = 1000;
		invoice.paid = false;
		when(mDatabase.selectInvoice(PAYMENT_ID)).thenReturn(invoice);
		
		LookupInvoice lookupInvoice = new LookupInvoice();
		lookupInvoice.amountPaidSat = 999L;
		lookupInvoice.status = InvoiceStatus.SETTLED;
		when(mLndNode.lookupInvoice(invoice)).thenReturn(lookupInvoice);
		
		boolean result = mCheckPaymentUseCase.isPaymentDone(PAYMENT_ID);
		
		assertFalse(result);
		verify(mDatabase).selectInvoice(PAYMENT_ID);
		verifyNoMoreInteractions(mDatabase);
		verify(mLndNode).lookupInvoice(Mockito.any());
		verifyNoMoreInteractions(mLndNode);
	}
	
	@Test
	public void testTestInvalidInvoice() throws Exception {
		when(mDatabase.selectInvoice(PAYMENT_ID)).thenReturn(null);
		
		try {
			mCheckPaymentUseCase.isPaymentDone(PAYMENT_ID);
			fail("Should have thrown");
		}
		catch (ServerException e) {}
	}
}
