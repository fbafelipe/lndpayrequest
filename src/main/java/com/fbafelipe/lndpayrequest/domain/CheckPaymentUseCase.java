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
	
	public CheckPaymentUseCase(
			Database database,
			LndNode lndNode
	) {
		mDatabase = database;
		mLndNode = lndNode;
	}
	
	public InvoiceStatus getPaymentStatus(String paymentId) throws ServerException {
		try {
			Invoice invoice = mDatabase.selectInvoice(paymentId);
			if (invoice == null)
				throw new ServerException(ServerError.BAD_REQUEST);
			
			if (invoice.status != InvoiceStatus.OPEN)
				return invoice.status;
			
			LookupInvoice lookupInvoice = mLndNode.lookupInvoice(invoice);
			if (lookupInvoice.status == InvoiceStatus.PAID && lookupInvoice.amountPaidSat >= invoice.amountSat) {
				invoice.status = InvoiceStatus.PAID;
				mDatabase.updateInvoiceStatus(invoice);
				return InvoiceStatus.PAID;
			}
			
			if (lookupInvoice.status == InvoiceStatus.TIMED_OUT) {
				invoice.status = InvoiceStatus.TIMED_OUT;
				mDatabase.updateInvoiceStatus(invoice);
				return InvoiceStatus.TIMED_OUT;
			}
			
			return InvoiceStatus.OPEN;
		}
		catch (SQLException e) {
			throw new ServerException(ServerError.DATABASE_ERROR, e);
		}
		catch (LndException e) {
			throw new ServerException(ServerError.LND_ERROR, e);
		}
	}
}
