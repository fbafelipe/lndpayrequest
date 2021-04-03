package com.fbafelipe.lndpayrequest.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.fbafelipe.lndpayrequest.data.Database;
import com.fbafelipe.lndpayrequest.data.PostPayloadReader;
import com.fbafelipe.lndpayrequest.data.ServerConfig;
import com.fbafelipe.lndpayrequest.di.ModuleFactory;
import com.fbafelipe.lndpayrequest.domain.model.Account;
import com.fbafelipe.lndpayrequest.domain.model.Currency;
import com.fbafelipe.lndpayrequest.domain.model.InvoiceStatus;
import com.fbafelipe.lndpayrequest.domain.model.PaymentRequest;
import com.fbafelipe.lndpayrequest.servlet.v1.PaymentRequestServlet;
import com.fbafelipe.lndpayrequest.servlet.v1.PaymentStatusServlet;
import com.fbafelipe.lndpayrequest.testutils.LndTestNode;
import com.fbafelipe.lndpayrequest.testutils.TestEnv;

public class RequestPaymentAndPayTest {
	private PaymentRequestServlet mPaymentRequestServlet;
	private PaymentStatusServlet mPaymentStatusServlet;
	
	private ServerConfig mServerConfig;
	private Database mDatabase;
	private PostPayloadReader mPostPayloadReader;
	
	private LndTestNode mClientLnd;
	
	@Before
	public void setUp() throws Exception {
		TestEnv.getInstance().reset();
		
		mPostPayloadReader = mock(PostPayloadReader.class);
		
		ModuleFactory moduleFactory = new MockModuleFactory();
		
		mPaymentRequestServlet = new PaymentRequestServlet(moduleFactory);
		mPaymentStatusServlet = new PaymentStatusServlet(moduleFactory);
		
		mServerConfig = moduleFactory.getServerConfig();
		mDatabase = moduleFactory.getDatabase();
		
		createMockUser();
		
		mClientLnd = TestEnv.getInstance().getClientNode();
	}
	
	@After
	public void tearDown() throws Exception {
		TestEnv.getInstance().reset();
	}
	
	@Test
	public void requestPaymentSatAndPay() throws Exception {
		PaymentRequest paymentRequest = requestPayment(Currency.SATOSHI, "1000");
		
		InvoiceStatus status = checkPaymentStatus(paymentRequest.paymentId);
		assertEquals(InvoiceStatus.OPEN, status);
		
		boolean paid = mClientLnd.payInvoice(paymentRequest.paymentRequest);
		assertTrue(paid);
		
		status = checkPaymentStatus(paymentRequest.paymentId);
		assertEquals(InvoiceStatus.PAID, status);
	}
	
	@Test
	public void requestPaymentFiatAndPay() throws Exception {
		PaymentRequest paymentRequest = requestPayment(Currency.BRL, "5");
		
		InvoiceStatus status = checkPaymentStatus(paymentRequest.paymentId);
		assertEquals(InvoiceStatus.OPEN, status);
		
		boolean paid = mClientLnd.payInvoice(paymentRequest.paymentRequest);
		assertTrue(paid);
		
		status = checkPaymentStatus(paymentRequest.paymentId);
		assertEquals(InvoiceStatus.PAID, status);
	}
	
	@Test
	public void requestPaymentSatAndTimeout() throws Exception {
		PaymentRequest paymentRequest = requestPayment(Currency.SATOSHI, "1000");
		
		InvoiceStatus status = checkPaymentStatus(paymentRequest.paymentId);
		assertEquals(InvoiceStatus.OPEN, status);
		
		Thread.sleep(mServerConfig.getPaymentRequestTimeout() + 1000);
		
		boolean paid = mClientLnd.payInvoice(paymentRequest.paymentRequest);
		assertFalse(paid);
		
		status = checkPaymentStatus(paymentRequest.paymentId);
		assertEquals(InvoiceStatus.TIMED_OUT, status);
	}
	
	private PaymentRequest requestPayment(Currency currency, String amount) throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		
		when(request.getMethod()).thenReturn("POST");
		when(mPostPayloadReader.readStringPayload(Mockito.any())).thenReturn(
				"{"
				+ "\"apikey\": \"testapikey\","
				+ "\"amount\": " + amount + ","
				+ "\"currency\": \"" + currency.identifier + "\""
				+ "}"
		);
		
		StringWriter stringWriter = new StringWriter();
		when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
		failOnError(response);
		
		mPaymentRequestServlet.service(request, response);
		
		JSONObject responseJson = new JSONObject(stringWriter.toString());
		PaymentRequest paymentRequest = new PaymentRequest();
		paymentRequest.paymentId = responseJson.getString("paymentId");
		paymentRequest.paymentRequest = responseJson.getString("paymentRequest");
		
		return paymentRequest;
	}
	
	private InvoiceStatus checkPaymentStatus(String paymentId) throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		
		when(request.getMethod()).thenReturn("GET");
		when(request.getPathInfo()).thenReturn("/" + paymentId);
		
		StringWriter stringWriter = new StringWriter();
		when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
		failOnError(response);
		
		mPaymentStatusServlet.service(request, response);
		
		JSONObject responseJson = new JSONObject(stringWriter.toString());
		switch (responseJson.getString("status")) {
			case "OPEN":
				return InvoiceStatus.OPEN;
			case "PAID":
				return InvoiceStatus.PAID;
			case "TIMED_OUT":
				return InvoiceStatus.TIMED_OUT;
		}
		throw new RuntimeException("Unknown status: " + responseJson.getString("status"));
	}
	
	private void failOnError(HttpServletResponse response) throws Exception {
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				fail();
				return null;
			}
			
		}).when(response).sendError(Mockito.anyInt());
	}
	
	private void createMockUser() throws Exception {
		Account user = new Account();
		user.username = "testuser";
		user.apikey = "testapikey";
		user.passwordHash = "disabled";
		user.passwordSalt = "disabled";
		mDatabase.insertAccount(user);
	}
	
	private class MockModuleFactory extends ModuleFactory {
		@Override
		public synchronized PostPayloadReader getPostPayloadReader() {
			return mPostPayloadReader;
		}
	}
}
