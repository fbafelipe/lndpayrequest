package com.fbafelipe.lndpayrequest.data;

import java.io.IOException;
import java.util.UUID;

import okhttp3.CertificatePinner;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import com.fbafelipe.lndpayrequest.domain.model.Invoice;
import com.fbafelipe.lndpayrequest.domain.model.InvoiceStatus;
import com.fbafelipe.lndpayrequest.domain.model.LookupInvoice;
import com.fbafelipe.lndpayrequest.exception.LndException;

public class LndNode {
	private static final MediaType MEDIA_TYPE_JSON = MediaType.get("application/json; charset=utf-8");
	
	private static final int HTTP_OK = 200;
	
	public LndNode(ServerConfig serverConfig) {
		mServerConfig = serverConfig;
	}
	
	private ServerConfig mServerConfig;
	private OkHttpClient mClient;
	
	public Invoice addInvoice(long amountSat) throws LndException {
		String path = "/v1/invoices";
		JSONObject body = new JSONObject();
		
		body.put("value", String.valueOf(amountSat));
		
		JSONObject response = restPost(path, body);
		
		try {
			Invoice invoice = new Invoice();
			invoice.paymentId = UUID.randomUUID().toString();
			invoice.rHash = response.getString("r_hash");
			invoice.paymentRequest = response.getString("payment_request");
			invoice.amountSat = amountSat;
			return invoice;
		}
		catch (JSONException e) {
			throw new LndException("Invalid LND response", e);
		}
	}
	
	public LookupInvoice lookupInvoice(Invoice invoice) throws LndException {
		String path = "/v1/invoice/" + invoice.rHash;
		
		JSONObject response = restGet(path);
		
		try {
			LookupInvoice lookupInvoice = new LookupInvoice();
			if ("SETTLED".equals(response.getString("state")))
				lookupInvoice.status = InvoiceStatus.SETTLED;
			else
				lookupInvoice.status = InvoiceStatus.OPEN;
			
			lookupInvoice.amountPaidSat = response.optLong("amt_paid_sat");
			
			return lookupInvoice;
		}
		catch (JSONException e) {
			throw new LndException("Invalid LND response", e);
		}
	}
	
	private JSONObject restPost(String path, JSONObject body) throws LndException {
		return restCall(path, body);
	}
	
	private JSONObject restGet(String path) throws LndException {
		return restCall(path, null);
	}
	
	private JSONObject restCall(String path, JSONObject body) throws LndException {
		try {
			Request.Builder requestBuilder = new Request.Builder();
			requestBuilder.url("https://" + mServerConfig.getLndRestHost() + ":" + mServerConfig.getLndRestPort() + path);
			requestBuilder.addHeader("Grpc-Metadata-macaroon", mServerConfig.getLndMacaroon());
			
			if (body != null) {
				RequestBody requestBody = RequestBody.create(body.toString(), MEDIA_TYPE_JSON);
				requestBuilder.post(requestBody);
			}
			else
				requestBuilder.get();
			
			Request request = requestBuilder.build();
			
			Response response = getClient().newCall(request).execute();
			if (response.code() != HTTP_OK)
				throw new IOException("Http status code " + response.code());
			
			return new JSONObject(response.body().string());
		}
		catch (Exception e) {
			throw new LndException(e.getMessage(), e);
		}
	}
	
	private synchronized OkHttpClient getClient() {
		if (mClient == null) {
			CertificatePinner certificatePinner = new CertificatePinner.Builder()
				.add(mServerConfig.getLndRestHost(), "sha256/" + mServerConfig.getLndHttpsCert())
				.build();
			
			mClient = new OkHttpClient.Builder()
				.certificatePinner(certificatePinner)
				.build();
		}
		
		return mClient;
	}
}
