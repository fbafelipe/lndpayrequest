package com.fbafelipe.lndpayrequest.servlet.v1;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.fbafelipe.lndpayrequest.di.ModuleFactory;
import com.fbafelipe.lndpayrequest.domain.CheckPaymentUseCase;
import com.fbafelipe.lndpayrequest.exception.ServerException;
import com.fbafelipe.lndpayrequest.util.Utils;

@WebServlet("/v1/paymentstatus/*")
public class PaymentStatusServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Pattern PAYMENT_ID_REGEX = Pattern.compile("/(\\w+)");
	
	private CheckPaymentUseCase mCheckPayment;
	
	public PaymentStatusServlet() {
    	this(ModuleFactory.getInstance());
    }
	
	public PaymentStatusServlet(ModuleFactory factory) {
		mCheckPayment = factory.getCheckPayment();
	}
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			Matcher matcher = PAYMENT_ID_REGEX.matcher(request.getPathInfo());
			if (matcher.matches()) {
				Utils.prepareResponse(response);
				
				String paymentId = matcher.group(1);
				
				JSONObject json = new JSONObject();
				json.put("paid", mCheckPayment.isPaymentDone(paymentId));
				
				PrintWriter output = response.getWriter();
				output.println(json.toString());
			}
			else
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		catch (ServerException e) {
			Utils.errorResponse(response, e);
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_BAD_REQUEST);
	}

}
