package com.fbafelipe.lndpayrequest.domain;

import java.sql.SQLException;

import com.fbafelipe.lndpayrequest.data.Clock;
import com.fbafelipe.lndpayrequest.data.Database;
import com.fbafelipe.lndpayrequest.data.LndNode;
import com.fbafelipe.lndpayrequest.domain.model.Currency;
import com.fbafelipe.lndpayrequest.domain.model.Invoice;
import com.fbafelipe.lndpayrequest.domain.model.PaymentRequest;
import com.fbafelipe.lndpayrequest.exception.LndException;
import com.fbafelipe.lndpayrequest.exception.ServerError;
import com.fbafelipe.lndpayrequest.exception.ServerException;

public class RequestPaymentUseCase {
	private Database mDatabase;
	private LndNode mLndNode;
	private Clock mClock;
	
	public RequestPaymentUseCase(Database database, LndNode lndNode, Clock clock) {
		mDatabase = database;
		mLndNode = lndNode;
		mClock = clock;
	}
	
	public PaymentRequest requestPayment(String apikey, long amount, Currency currency) throws ServerException {
		try {
			Long userId = mDatabase.selectUserIdFromApikey(apikey);
			if (userId == null)
				throw new ServerException(ServerError.BAD_REQUEST);
			
			if (amount <= 0)
				throw new ServerException(ServerError.BAD_REQUEST);
			
			Invoice invoice = mLndNode.addInvoice(amount);
			invoice.userId = userId;
			invoice.date = mClock.currentTimeMillis();
			invoice.paid = false;
			
			mDatabase.insertInvoice(invoice);
			
			PaymentRequest paymentRequest = new PaymentRequest();
			paymentRequest.paymentId = invoice.paymentId;
			paymentRequest.paymentRequest = invoice.paymentRequest;
			return paymentRequest;
		}
		catch (SQLException e) {
			throw new ServerException(ServerError.DATABASE_ERROR);
		}
		catch (LndException e) {
			throw new ServerException(ServerError.LND_ERROR);
		}
	}
}
