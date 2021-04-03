package com.fbafelipe.lndpayrequest.servlet.v1;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import com.fbafelipe.lndpayrequest.domain.model.InvoiceStatus;
import com.fbafelipe.lndpayrequest.util.Utils;

@WebServlet("/v1/paymentstatus/*")
public class PaymentStatusServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOGGER = Logger.getLogger(PaymentStatusServlet.class.getSimpleName());
	
	private static final Pattern PAYMENT_ID_REGEX = Pattern.compile("/([\\w-]+)");
	
	private CheckPaymentUseCase mCheckPayment;
	
	public PaymentStatusServlet() {
    	this(ModuleFactory.getInstance());
    }
	
	public PaymentStatusServlet(ModuleFactory factory) {
		mCheckPayment = factory.getCheckPayment();
	}
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOGGER.log(Level.INFO, "Serving GET request from " + request.getRemoteAddr());
		
		try {
			String paymentId = getPaymentId(request.getPathInfo());
			if (paymentId != null) {
				Utils.prepareResponse(response);
				
				InvoiceStatus status = mCheckPayment.getPaymentStatus(paymentId);
				
				JSONObject json = new JSONObject();
				json.put("status", status.toString());
				
				PrintWriter output = response.getWriter();
				output.println(json.toString());
				
				LOGGER.log(Level.INFO, request.getRemoteAddr() + " is checking if paymentId=" + paymentId + " is paid. status=" + status);
			}
			else {
				LOGGER.log(Level.WARNING, request.getRemoteAddr() + " tried to check an invalid paymentId");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
		}
		catch (Exception e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
			Utils.errorResponse(response, e);
		}
	}
	
	private String getPaymentId(String pathInfo) {
		if (pathInfo == null)
			return null;
		
		Matcher matcher = PAYMENT_ID_REGEX.matcher(pathInfo);
		return matcher.matches() ? matcher.group(1) : null;
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOGGER.log(Level.INFO, "Serving POST request from " + request.getRemoteAddr());
		
		response.sendError(HttpServletResponse.SC_BAD_REQUEST);
	}

}
