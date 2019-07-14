package com.fbafelipe.lndpayrequest.domain;

public class RequestPaymentUseCase {
	public PaymentRequest requestPayment(String apikey, long amount) {
		// TODO
		PaymentRequest paymentRequest = new PaymentRequest();
		paymentRequest.paymentId = "12345";
		paymentRequest.paymentRequest = "lnblablabla";
		return paymentRequest;
	}
}
