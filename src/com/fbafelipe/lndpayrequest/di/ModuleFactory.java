package com.fbafelipe.lndpayrequest.di;

import com.fbafelipe.lndpayrequest.data.Clock;
import com.fbafelipe.lndpayrequest.data.Database;
import com.fbafelipe.lndpayrequest.data.LndNode;
import com.fbafelipe.lndpayrequest.data.PostPayloadReader;
import com.fbafelipe.lndpayrequest.data.ServerConfig;
import com.fbafelipe.lndpayrequest.domain.CheckPaymentUseCase;
import com.fbafelipe.lndpayrequest.domain.RequestPaymentUseCase;

public class ModuleFactory {
	private static ModuleFactory sInstance = new ModuleFactory();
	
	// domain
	private RequestPaymentUseCase mRequestPayment;
	private CheckPaymentUseCase mCheckPayment;
	
	// data
	private Clock mClock;
	private Database mDatabase;
	private LndNode mLndNode;
	private PostPayloadReader mPostPayloadReader;
	private ServerConfig mServerConfig;
	
	public static ModuleFactory getInstance() {
		return sInstance;
	}

	public synchronized RequestPaymentUseCase getRequestPayment() {
		if (mRequestPayment == null)
			mRequestPayment = new RequestPaymentUseCase(
				getDatabase(),
				getLndNode(),
				getClock()
			);
		
		return mRequestPayment;
	}

	public synchronized CheckPaymentUseCase getCheckPayment() {
		if (mCheckPayment == null)
			mCheckPayment = new CheckPaymentUseCase(
				getDatabase(),
				getLndNode()
			);
		
		return mCheckPayment;
	}
	
	public synchronized Clock getClock() {
		if (mClock == null)
			mClock = new Clock();
		
		return mClock;
	}
	
	public synchronized Database getDatabase() {
		if (mDatabase == null)
			mDatabase = new Database(getServerConfig());
		
		return mDatabase;
	}
	
	public synchronized LndNode getLndNode() {
		if (mLndNode == null)
			mLndNode = new LndNode(getServerConfig());
		
		return mLndNode;
	}

	public synchronized PostPayloadReader getPostPayloadReader() {
		if (mPostPayloadReader == null)
			mPostPayloadReader = new PostPayloadReader();
		
		return mPostPayloadReader;
	}
	
	public synchronized ServerConfig getServerConfig() {
		if (mServerConfig == null)
			mServerConfig = new ServerConfig();
		
		return mServerConfig;
	}
}
