package com.fbafelipe.lndpayrequest.domain.model;

public class Invoice {
	public String paymentId;
	public long userId;
	public String rHash;
	public String paymentRequest;
	public long amountSat;
	public long date;
	public boolean paid;
}
