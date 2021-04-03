package com.fbafelipe.lndpayrequest.domain.model;

public enum InvoiceStatus {
	OPEN(0),
	PAID(1),
	TIMED_OUT(2);
	
	public final int id;
	
	public static InvoiceStatus fromId(int id) {
		for (InvoiceStatus value : values()) {
			if (id == value.id)
				return value;
		}
		return OPEN; // default to OPEN
	}
	
	private InvoiceStatus(int id) {
		this.id = id;
	}
}
