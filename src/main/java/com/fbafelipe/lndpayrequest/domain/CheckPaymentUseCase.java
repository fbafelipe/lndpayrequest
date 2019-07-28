package com.fbafelipe.lndpayrequest.domain;

import java.sql.SQLException;

import com.fbafelipe.lndpayrequest.data.Database;
import com.fbafelipe.lndpayrequest.data.LndNode;
import com.fbafelipe.lndpayrequest.domain.model.Invoice;
import com.fbafelipe.lndpayrequest.domain.model.InvoiceStatus;
import com.fbafelipe.lndpayrequest.domain.model.LookupInvoice;
import com.fbafelipe.lndpayrequest.exception.LndException;
import com.fbafelipe.lndpayrequest.exception.ServerError;
import com.fbafelipe.lndpayrequest.exception.ServerException;

public class CheckPaymentUseCase {
	private Database mDatabase;
	private LndNode mLndNode;
	
	public CheckPaymentUseCase(Database database, LndNode lndNode) {
		mDatabase = database;
		mLndNode = lndNode;
	}
	
	public boolean isPaymentDone(String paymentId) throws ServerException {
		try {
			Invoice invoice = mDatabase.selectInvoice(paymentId);
			if (invoice == null)
				throw new ServerException(ServerError.BAD_REQUEST);
			
			if (invoice.paid)
				return true;
			
			LookupInvoice lookupInvoice = mLndNode.lookupInvoice(invoice);
			if (lookupInvoice.status == InvoiceStatus.SETTLED && lookupInvoice.amountPaidSat >= invoice.amountSat) {
				invoice.paid = true;
				mDatabase.updatePaidInvoice(invoice);
				return true;
			}
			
			return false;
		}
		catch (SQLException e) {
			throw new ServerException(ServerError.DATABASE_ERROR, e);
		}
		catch (LndException e) {
			throw new ServerException(ServerError.LND_ERROR, e);
		}
	}
}
