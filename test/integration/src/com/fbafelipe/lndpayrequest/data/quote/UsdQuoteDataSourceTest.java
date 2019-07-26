package com.fbafelipe.lndpayrequest.data.quote;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.fbafelipe.lndpayrequest.data.OkHttpClientFactory;

public class UsdQuoteDataSourceTest {
	private UsdQuoteDataSource mQuoteDataSource;
	
	@Before
	public void setUp() {
		mQuoteDataSource = new UsdQuoteDataSource(new OkHttpClientFactory());
	}
	
	@Test
	public void testGetBitcoinValue() {
		Double value = mQuoteDataSource.getBitcoinValue();
		
		assertNotNull(value);
		assertTrue(value > 0.0);
	}
}
