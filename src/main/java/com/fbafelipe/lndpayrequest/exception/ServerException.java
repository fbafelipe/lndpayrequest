package com.fbafelipe.lndpayrequest.exception;

public class ServerException extends Exception {
	private ServerError mError;
	
	public ServerException(ServerError error) {
		super(error.name());
		mError = error;
	}
	
	public ServerException(ServerError error, Throwable cause) {
		super(error.name(), cause);
		mError = error;
	}
	
	public ServerError getError() {
		return mError;
	}
}
