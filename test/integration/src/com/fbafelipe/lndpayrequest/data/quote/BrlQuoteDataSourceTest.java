package com.fbafelipe.lndpayrequest.data.quote;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.fbafelipe.lndpayrequest.data.OkHttpClientFactory;

public class BrlQuoteDataSourceTest {
	private BrlQuoteDataSource mQuoteDataSource;
	
	@Before
	public void setUp() {
		mQuoteDataSource = new BrlQuoteDataSource(new OkHttpClientFactory());
	}
	
	@Test
	public void testGetBitcoinValue() {
		Double value = mQuoteDataSource.getBitcoinValue();
		
		assertNotNull(value);
		assertTrue(value > 0.0);
	}
}
