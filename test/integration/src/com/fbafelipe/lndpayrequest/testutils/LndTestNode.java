package com.fbafelipe.lndpayrequest.testutils;

import java.io.File;

import org.json.JSONObject;

public class LndTestNode {
	private File mWorkingDir;
	private File mScriptFile;
	
	public LndTestNode(File workingDir, File scriptFile) {
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
			return (JSONObject) new JSONObject(result);
		}
		catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
