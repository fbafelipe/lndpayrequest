package com.fbafelipe.lndpayrequest.data.quote;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONObject;

import com.fbafelipe.lndpayrequest.data.OkHttpClientFactory;


public class BrlQuoteDataSource implements QuoteDataSource {
	private static final Logger LOGGER = Logger.getLogger(BrlQuoteDataSource.class.getSimpleName());
	
	private static final String URL = "https://www.mercadobitcoin.net/api/BTC/ticker";
	
	private OkHttpClient mClient;
	
	public BrlQuoteDataSource(OkHttpClientFactory clientFactory) {
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
			double value = json.getJSONObject("ticker").getDouble("last");
			
			if (value <= 0.0)
				throw new IllegalArgumentException("Invalid value " + value);
			
			return value;
		}
		catch (Exception e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
			return null;
		}
	}
}
