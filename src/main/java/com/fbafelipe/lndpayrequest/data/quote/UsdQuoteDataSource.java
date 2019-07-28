package com.fbafelipe.lndpayrequest.data.quote;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.fbafelipe.lndpayrequest.data.OkHttpClientFactory;


public class UsdQuoteDataSource implements QuoteDataSource {
	private static final String URL = "https://api.coinbase.com/v2/prices/BTC-USD/spot";
	
	private OkHttpClient mClient;
	
	public UsdQuoteDataSource(OkHttpClientFactory clientFactory) {
		mClient = clientFactory.createOkHttpClient();
	}
	
	@Override
	public Double getBitcoinValue() {
		Request request = new Request.Builder()
		.get()
		.url(URL)
		.build();
	
	try {
		Response response = mClient.newCall(request).execute();
		if (response.code() != HttpURLConnection.HTTP_OK)
			throw new IOException("Http status code " + response.code());
		
		JSONObject json = new JSONObject(response.body().string());
		double value = json.getJSONObject("data").getDouble("amount");
		
		if (value <= 0.0)
			throw new IllegalArgumentException("Invalid value " + value);
		
		return value;
	}
	catch (Exception e) {
		// TODO log exception
		return null;
	}
	}
}
