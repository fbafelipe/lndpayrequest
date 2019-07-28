package com.fbafelipe.lndpayrequest.domain.model;

public enum Currency {
	SATOSHI("sat"),
	USD("USD"),
	BRL("BRL");
	
	public final String identifier;
	
	public static Currency fromIdentifier(String id) {
		for (Currency currency : values()) {
			if (currency.identifier.equals(id))
				return currency;
		}
		
		return null;
	}
	
	private Currency(String id) {
		identifier = id;
	}
}
