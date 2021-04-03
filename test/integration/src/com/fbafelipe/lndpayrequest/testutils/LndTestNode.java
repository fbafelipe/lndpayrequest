package com.fbafelipe.lndpayrequest.testutils;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

public class LndTestNode {
	private File mWorkingDir;
	private File mScriptFile;
	
	public LndTestNode(File workingDir, File scriptFile) {
		mWorkingDir = workingDir;
		mScriptFile = scriptFile;
	}
	
	public boolean payInvoice(String paymentRequest) {
		String response = CommandUtils.runCommand(mWorkingDir, mScriptFile, "payinvoice", "-f", "--json", paymentRequest);
		
		try {
			JSONObject json = new JSONObject(response);
			return json.getString("status").equals("SUCCEEDED");
		}
		catch (JSONException e) {
			if (response.contains("invoice expired"))
				return false;
			
			throw new RuntimeException(e.getMessage(), e);
		}
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
