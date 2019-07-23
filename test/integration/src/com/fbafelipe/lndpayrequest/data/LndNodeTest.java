package com.fbafelipe.lndpayrequest.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import javax.net.ssl.SSLPeerUnverifiedException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.fbafelipe.lndpayrequest.di.ModuleFactory;
import com.fbafelipe.lndpayrequest.domain.model.Invoice;
import com.fbafelipe.lndpayrequest.domain.model.InvoiceStatus;
import com.fbafelipe.lndpayrequest.domain.model.LookupInvoice;
import com.fbafelipe.lndpayrequest.exception.LndException;
import com.fbafelipe.lndpayrequest.testutils.LndTestNode;
import com.fbafelipe.lndpayrequest.testutils.TestEnv;
import com.fbafelipe.lndpayrequest.util.BinaryUtils;

public class LndNodeTest {
	private LndNode mLndNode;
	
	private LndTestNode mClientLnd;
	private LndTestNode mServerLnd;
	
	@Before
	public void setUp() throws Exception {
		TestEnv.getInstance().reset();
		
		ModuleFactory moduleFactory = new ModuleFactory();
		
		mLndNode = moduleFactory.getLndNode();
		
		mClientLnd = TestEnv.getInstance().getClientNode();
		mServerLnd = TestEnv.getInstance().getServerNode();
	}
	
	@After
	public void tearDown() throws Exception {
		TestEnv.getInstance().reset();
	}
	
	@Test
	public void testAddInvoice() throws Exception {
		Invoice invoice = mLndNode.addInvoice(1000);
		
		assertNotNull(invoice.paymentId);
		assertNotNull(invoice.paymentRequest);
		assertNotNull(invoice.rHash);
		assertEquals(1000, invoice.amountSat);
		
		assertTrue(mServerLnd.isInvoiceOpen(BinaryUtils.base64ToHex(invoice.rHash)));
	}
	
	@Test
	public void testLookupInvoice() throws Exception {
		Invoice invoice = mLndNode.addInvoice(1000);
		LookupInvoice lookupInvoice = mLndNode.lookupInvoice(invoice);
		
		assertEquals(InvoiceStatus.OPEN, lookupInvoice.status);
		assertEquals(0L, (long) lookupInvoice.amountPaidSat);
		
		mClientLnd.payInvoice(invoice.paymentRequest);
		
		lookupInvoice = mLndNode.lookupInvoice(invoice);
		
		assertEquals(InvoiceStatus.SETTLED, lookupInvoice.status);
		assertEquals(1000L, (long) lookupInvoice.amountPaidSat);
	}
	
	@Test
	public void testWrongSslCert() throws Exception {
		ServerConfig originalServerConfig = new ModuleFactory().getServerConfig();
		ServerConfig serverConfig = Mockito.mock(ServerConfig.class);
		
		when(serverConfig.getLndMacaroon()).thenReturn(originalServerConfig.getLndMacaroon());
		when(serverConfig.getLndRestHost()).thenReturn(originalServerConfig.getLndRestHost());
		when(serverConfig.getLndRestPort()).thenReturn(originalServerConfig.getLndRestPort());
		
		// return client fingerprint instead
		when(serverConfig.getLndHttpsCert()).thenReturn("9D:4B:C0:A0:D8:8B:00:9A:3A:AD:A8:14:BD:57:7E:81:CD:41:90:6B:3C:10:BF:71:7A:5A:3E:E3:05:0A:E0:CB");
		
		mLndNode = new LndNode(serverConfig, new OkHttpClientFactory());
		
		try {
			mLndNode.addInvoice(1000);
			fail("Should have thrown");
		}
		catch (LndException e) {
			assertTrue(e.getCause() instanceof SSLPeerUnverifiedException);
		}
	}
}
