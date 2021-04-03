package com.fbafelipe.lndpayrequest.domain;

import java.sql.SQLException;

import com.fbafelipe.lndpayrequest.data.Clock;
import com.fbafelipe.lndpayrequest.data.Database;
import com.fbafelipe.lndpayrequest.data.LndNode;
import com.fbafelipe.lndpayrequest.data.quote.QuoteRepository;
import com.fbafelipe.lndpayrequest.domain.model.Currency;
import com.fbafelipe.lndpayrequest.domain.model.Invoice;
import com.fbafelipe.lndpayrequest.domain.model.InvoiceStatus;
import com.fbafelipe.lndpayrequest.domain.model.PaymentRequest;
import com.fbafelipe.lndpayrequest.exception.LndException;
import com.fbafelipe.lndpayrequest.exception.ServerError;
import com.fbafelipe.lndpayrequest.exception.ServerException;

public class RequestPaymentUseCase {
	private static final long SATOSHI_BITCOIN_VALUE = 100000000L;
	
	private static final long MAX_PAYMENT_VALUE_SAT = 10000000L;
	
	private Database mDatabase;
	private QuoteRepository mQuoteRepository;
	private LndNode mLndNode;
	private Clock mClock;
	
	public RequestPaymentUseCase(
			Database database,
			QuoteRepository quoteRepository,
			LndNode lndNode,
			Clock clock
	) {
		mDatabase = database;
		mQuoteRepository = quoteRepository;
		mLndNode = lndNode;
		mClock = clock;
	}
	
	public PaymentRequest requestPayment(String apikey, Number amount, Currency currency) throws ServerException {
		try {
			Long accountId = mDatabase.selectAccountIdFromApikey(apikey);
			if (accountId == null)
				throw new ServerException(ServerError.BAD_REQUEST);
			
			long amountSat = convertToSatoshi(amount, currency);
			
			if (amountSat <= 0 || amountSat > MAX_PAYMENT_VALUE_SAT)
				throw new ServerException(ServerError.BAD_REQUEST);
			
			Invoice invoice = mLndNode.addInvoice(amountSat);
			invoice.accountId = accountId;
			invoice.date = mClock.currentTimeMillis();
			invoice.status = InvoiceStatus.OPEN;
			
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
	
	private long convertToSatoshi(Number amount, Currency currency) throws ServerException {
		if (amount instanceof Double && !Double.isFinite((Double) amount))
			throw new ServerException(ServerError.BAD_REQUEST);
		
		if (currency == Currency.SATOSHI)
			return amount.longValue();
		
		double bitcoinValue = mQuoteRepository.getBitcoinValue(currency);
		long amountSat = Math.round(amount.doubleValue() / bitcoinValue * SATOSHI_BITCOIN_VALUE);
		return amountSat;
	}
}
