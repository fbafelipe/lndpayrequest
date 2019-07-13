package com.fbafelipe.lndpayrequest.test;

import java.io.File;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class LndNode {
	private File mWorkingDir;
	private File mScriptFile;
	
	public LndNode(File workingDir, File scriptFile) {
		mWorkingDir = workingDir;
		mScriptFile = scriptFile;
	}
	
	public boolean payInvoice(String paymentRequest) {
		JSONObject response = runCommand("payinvoice", "-f", paymentRequest);
		
		Object preimage = response.get("payment_preimage");
		return preimage instanceof String && !((String) preimage).isEmpty();
	}
	
	public boolean isInvoicePaid(String rhash) {
		JSONObject response = runCommand("lookupinvoice", rhash);
		
		return "SETTLED".equals(response.get("state"));
	}
	
	public boolean isInvoiceOpen(String rhash) {
		JSONObject response = runCommand("lookupinvoice", rhash);
		
		return "OPEN".equals(response.get("state"));
	}
	
	private JSONObject runCommand(String ... args) {
		try {
			String result = CommandUtils.runCommand(mWorkingDir, mScriptFile, args);
			return (JSONObject) new JSONParser().parse(result);
		}
		catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
