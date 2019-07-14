package com.fbafelipe.lndpayrequest.servlet.v1;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.fbafelipe.lndpayrequest.data.PostPayloadReader;
import com.fbafelipe.lndpayrequest.domain.PaymentRequest;
import com.fbafelipe.lndpayrequest.domain.RequestPaymentUseCase;
import com.fbafelipe.lndpayrequest.util.Utils;

@WebServlet("/v1/paymentrequest")
public class PaymentRequestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private RequestPaymentUseCase mRequestPayment;
	private PostPayloadReader mPostPayloadReader;
	
    public PaymentRequestServlet() {
    	mRequestPayment = new RequestPaymentUseCase();
    	mPostPayloadReader = new PostPayloadReader();
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_BAD_REQUEST);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String apikey;
		long amount;
		
		try {
			String body = mPostPayloadReader.readStringPayload(request);
			JSONObject json = new JSONObject(body);
			
			apikey = json.getString("apikey");
			amount = json.getLong("amount");
		}
		catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		PaymentRequest paymentRequest = mRequestPayment.requestPayment(apikey, amount);
		JSONObject json = new JSONObject();
		json.put("paymentId", paymentRequest.paymentId);
		json.put("paymentRequest", paymentRequest.paymentRequest);
		
		Utils.prepareResponse(response);
		PrintWriter output = response.getWriter();
		output.println(json.toString());
	}
	
	
}
