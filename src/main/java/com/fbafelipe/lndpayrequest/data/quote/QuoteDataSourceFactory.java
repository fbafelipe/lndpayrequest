package com.fbafelipe.lndpayrequest.data.quote;

import java.util.HashMap;
import java.util.Map;

import com.fbafelipe.lndpayrequest.data.OkHttpClientFactory;
import com.fbafelipe.lndpayrequest.domain.model.Currency;

public class QuoteDataSourceFactory {
	private OkHttpClientFactory mOkHttpClientFactory;
	
	public QuoteDataSourceFactory(OkHttpClientFactory okHttpClientFactory) {
		mOkHttpClientFactory = okHttpClientFactory;
	}
	
	public Map<Currency, QuoteDataSource> createQuoteDataSources() {
		Map<Currency, QuoteDataSource> map = new HashMap<>();
		
		map.put(Currency.BRL, new BrlQuoteDataSource(mOkHttpClientFactory));
		map.put(Currency.USD, new UsdQuoteDataSource(mOkHttpClientFactory));
		
		return map;
	}
}
