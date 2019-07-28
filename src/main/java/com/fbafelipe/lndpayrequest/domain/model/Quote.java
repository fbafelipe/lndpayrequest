package com.fbafelipe.lndpayrequest.domain.model;

public class Quote {
	public Currency currency;
	
	// Bitcoin value in the given currency
	public double bitcoinValue;
	
	public long lastUpdate;
	
	public Quote() {}
	
	public Quote(Currency currency, double bitcoinValue, long lastUpdate) {
		this.currency = currency;
		this.bitcoinValue = bitcoinValue;
		this.lastUpdate = lastUpdate;
	}
}
