package com.fbafelipe.lndpayrequest.servlet.v1;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.fbafelipe.lndpayrequest.data.PostPayloadReader;
import com.fbafelipe.lndpayrequest.di.ModuleFactory;
import com.fbafelipe.lndpayrequest.domain.RequestPaymentUseCase;
import com.fbafelipe.lndpayrequest.domain.model.Currency;
import com.fbafelipe.lndpayrequest.domain.model.PaymentRequest;
import com.fbafelipe.lndpayrequest.exception.ServerError;
import com.fbafelipe.lndpayrequest.exception.ServerException;
import com.fbafelipe.lndpayrequest.util.Utils;

@WebServlet("/v1/paymentrequests")
public class PaymentRequestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOGGER = Logger.getLogger(PaymentRequestServlet.class.getSimpleName());
	
	private RequestPaymentUseCase mRequestPayment;
	private PostPayloadReader mPostPayloadReader;
	
    public PaymentRequestServlet() {
    	this(ModuleFactory.getInstance());
    }
    
    public PaymentRequestServlet(ModuleFactory factory) {
    	mRequestPayment = factory.getRequestPayment();
    	mPostPayloadReader = factory.getPostPayloadReader();
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOGGER.log(Level.INFO, "Serving GET request from " + request.getRemoteAddr());
		
		response.sendError(HttpServletResponse.SC_BAD_REQUEST);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOGGER.log(Level.INFO, "Serving POST request from " + request.getRemoteAddr());
		
		String apikey;
		Number amount;
		Currency currency;
		
		try {
			String body = mPostPayloadReader.readStringPayload(request);
			JSONObject json = new JSONObject(body);
			
			apikey = json.getString("apikey");
			amount = json.getNumber("amount");
			currency = Currency.fromIdentifier(json.getString("currency"));
			
			if (currency == null)
				throw new ServerException(ServerError.BAD_REQUEST);
			
			PaymentRequest paymentRequest = mRequestPayment.requestPayment(apikey, amount, currency);
			json = new JSONObject();
			json.put("paymentId", paymentRequest.paymentId);
			json.put("paymentRequest", paymentRequest.paymentRequest);
			
			Utils.prepareResponse(response);
			PrintWriter output = response.getWriter();
			output.println(json.toString());
			
			LOGGER.log(Level.INFO, request.getRemoteAddr() + " requested payment of " + amount + " " + currency + ". paymentId=" + paymentRequest.paymentId);
		}
		catch (JSONException e) {
			LOGGER.log(Level.INFO, e.getMessage(), e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		catch (Exception e) {
			LOGGER.log(Level.INFO, e.getMessage(), e);
			Utils.errorResponse(response, e);
		}
	}
}
