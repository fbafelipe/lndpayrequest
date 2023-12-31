package com.fbafelipe.lndpayrequest.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import com.fbafelipe.lndpayrequest.domain.model.Account;
import com.fbafelipe.lndpayrequest.domain.model.Currency;
import com.fbafelipe.lndpayrequest.domain.model.Invoice;
import com.fbafelipe.lndpayrequest.domain.model.InvoiceStatus;
import com.fbafelipe.lndpayrequest.domain.model.Quote;
import com.fbafelipe.lndpayrequest.domain.model.Withdraw;
import com.fbafelipe.lndpayrequest.util.Utils;

public class Database {
	private ServerConfig mServerConfig;
	
	private Connection mConnection;
	
	private boolean mTablesCreated = false;
	
	private PreparedStatement mSelectInvoiceStmt;
	private PreparedStatement mInsertInvoiceStmt;
	private PreparedStatement mUpdateInvoiceStatusStmt;
	
	private PreparedStatement mSelectAccountIdFromApikeyStmt;
	private PreparedStatement mInsertAccountStmt;
	
	private PreparedStatement mSelectAccountInvoiceAmountSumStmt;
	private PreparedStatement mSelectAccountWithdrawAmountSumStmt;
	private PreparedStatement mInsertWithdrawStmt;
	
	private PreparedStatement mSelectQuoteStmt;
	private PreparedStatement mUpdateQuoteStmt;
	
	public Database(ServerConfig serverConfig) {
		mServerConfig = serverConfig;
	}
	
	public synchronized Invoice selectInvoice(String paymentId) throws SQLException {
		getConnection();
		mSelectInvoiceStmt.setString(1, paymentId);
		mSelectInvoiceStmt.execute();
		
		try (ResultSet resultSet = mSelectInvoiceStmt.getResultSet()) {
			if (resultSet.next()) {
				Invoice invoice = new Invoice();
				invoice.paymentId = paymentId;
				invoice.accountId = resultSet.getLong(1);
				invoice.rHash = resultSet.getString(2);
				invoice.paymentRequest = resultSet.getString(3);
				invoice.amountSat = resultSet.getLong(4);
				invoice.date = resultSet.getTimestamp(5).getTime();
				invoice.status = InvoiceStatus.fromId(resultSet.getInt(6));
				return invoice;
			}
			
			return null;
		}
	}
	
	public synchronized void insertInvoice(Invoice invoice) throws SQLException {
		getConnection();
		mInsertInvoiceStmt.setString(1, invoice.paymentId);
		mInsertInvoiceStmt.setLong(2, invoice.accountId);
		mInsertInvoiceStmt.setString(3, invoice.rHash);
		mInsertInvoiceStmt.setString(4, invoice.paymentRequest);
		mInsertInvoiceStmt.setLong(5, invoice.amountSat);
		mInsertInvoiceStmt.setTimestamp(6, new Timestamp(invoice.date));
		mInsertInvoiceStmt.setInt(7, invoice.status.id);
		
		mInsertInvoiceStmt.execute();
		
		// just to force a throw if somehow the insert failed
		getGeneratedKey(mInsertInvoiceStmt);
	}
	
	public synchronized void updateInvoiceStatus(Invoice invoice) throws SQLException {
		getConnection();
		mUpdateInvoiceStatusStmt.setInt(1, invoice.status.id);
		mUpdateInvoiceStatusStmt.setString(2, invoice.paymentId);
		
		mUpdateInvoiceStatusStmt.execute();
	}
	
	public synchronized Long selectAccountIdFromApikey(String apikey) throws SQLException {
		getConnection();
		mSelectAccountIdFromApikeyStmt.setString(1, apikey);
		mSelectAccountIdFromApikeyStmt.execute();
		
		try (ResultSet resultSet = mSelectAccountIdFromApikeyStmt.getResultSet()) {
			if (resultSet.next()) {
				return resultSet.getLong(1);
			}
			
			return null;
		}
	}
	
	public synchronized void insertAccount(Account user) throws SQLException {
		getConnection();
		mInsertAccountStmt.setString(1, user.username);
		mInsertAccountStmt.setString(2, user.apikey);
		mInsertAccountStmt.setString(3, user.passwordHash);
		mInsertAccountStmt.setString(4, user.passwordSalt);
		mInsertAccountStmt.execute();
		
		user.id = getGeneratedKey(mInsertAccountStmt);
	}
	
	public synchronized long selectAccountInvoiceAmountSum(long accountId) throws SQLException {
		getConnection();
		mSelectAccountInvoiceAmountSumStmt.setLong(1, accountId);
		mSelectAccountInvoiceAmountSumStmt.setInt(2, InvoiceStatus.PAID.id);
		mSelectAccountInvoiceAmountSumStmt.execute();
		
		try (ResultSet resultSet = mSelectAccountInvoiceAmountSumStmt.getResultSet()) {
			if (resultSet.next()) {
				return resultSet.getLong(1);
			}
			
			return 0L;
		}
	}
	
	public synchronized long selectAccountWithdrawAmountSum(long accountId) throws SQLException {
		getConnection();
		mSelectAccountWithdrawAmountSumStmt.setLong(1, accountId);
		mSelectAccountWithdrawAmountSumStmt.execute();
		
		try (ResultSet resultSet = mSelectAccountWithdrawAmountSumStmt.getResultSet()) {
			if (resultSet.next()) {
				return resultSet.getLong(1);
			}
			
			return 0L;
		}
	}
	
	public synchronized void insertWithdraw(Withdraw withdraw) throws SQLException {
		getConnection();
		mInsertWithdrawStmt.setLong(1, withdraw.accountId);
		mInsertWithdrawStmt.setLong(2, withdraw.amountSat);
		mInsertWithdrawStmt.setTimestamp(3, new Timestamp(withdraw.date));
		mInsertWithdrawStmt.execute();
		
		// just to force a throw if somehow the insert failed
		getGeneratedKey(mInsertWithdrawStmt);
	}
	
	public synchronized Quote selectQuote(Currency currency) throws SQLException {
		getConnection();
		mSelectQuoteStmt.setString(1, currency.identifier);
		
		mSelectQuoteStmt.execute();
		
		try (ResultSet resultSet = mSelectQuoteStmt.getResultSet()) {
			if (resultSet.next()) {
				Quote quote = new Quote();
				quote.currency = currency;
				quote.bitcoinValue = resultSet.getDouble(1);
				quote.lastUpdate = resultSet.getTimestamp(2).getTime();
				return quote;
			}
			
			return null;
		}
	}
	
	public synchronized void updateQuote(Quote quote) throws SQLException {
		getConnection();
		
		Timestamp lastUpdate = new Timestamp(quote.lastUpdate);
		
		mUpdateQuoteStmt.setString(1, quote.currency.identifier);
		mUpdateQuoteStmt.setDouble(2, quote.bitcoinValue);
		mUpdateQuoteStmt.setTimestamp(3, lastUpdate);
		mUpdateQuoteStmt.setDouble(4, quote.bitcoinValue);
		mUpdateQuoteStmt.setTimestamp(5, lastUpdate);
		
		mUpdateQuoteStmt.execute();
	}
	
	private synchronized Connection getConnection() throws SQLException {
		if (!isAlive()) {
			disconnect();
			connect();
		}
		
		return mConnection;
	}
	
	private synchronized void connect() throws SQLException {
		try {
			Class.forName(mServerConfig.getJdbcDriver());
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		
		mConnection = DriverManager.getConnection(mServerConfig.getJdbcUrl(), mServerConfig.getJdbcUsername(), mServerConfig.getJdbcPassword());
		
		createTables(mConnection);
		compileStatements(mConnection);
	}
	
	private synchronized void disconnect() throws SQLException {
		if (mConnection != null) {
			mConnection.close();
			mConnection = null;
		}
	}
	
	private synchronized boolean isAlive() {
		try {
			return mConnection != null && mConnection.isValid(0);
		}
		catch (Exception error) {
			return false;
		}
	}
	
	private int getGeneratedKey(PreparedStatement stmt) throws SQLException {
		try (ResultSet key = stmt.getGeneratedKeys()) {
			if (!key.next())
				throw new SQLException("Error getting generated key.");
			return key.getInt(1);
		}
	}
	
	private void createTables(Connection connection) throws SQLException {
		if (mTablesCreated)
			return;
		mTablesCreated = true;
		
		String sql = Utils.readResource("create_tables.sql");
		String sqlCommands[] = sql.split(";");
		
		Statement statement = connection.createStatement();
		for (String cmd : sqlCommands) {
			if (cmd.trim().isEmpty())
				continue;
			statement.execute(cmd + ";");
		}
	}
	
	private void compileStatements(Connection connection) throws SQLException {
		mSelectInvoiceStmt = connection.prepareStatement("SELECT"
				+ " accountId, rHash, paymentRequest, amountSat, date, status"
				+ " FROM Invoice WHERE paymentId=?");
		
		mInsertInvoiceStmt = connection.prepareStatement("INSERT INTO Invoice"
				+ " (paymentId, accountId, rHash, paymentRequest, amountSat, date, status)"
				+ " VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
		
		mUpdateInvoiceStatusStmt = connection.prepareStatement("UPDATE Invoice"
				+ " SET status=? WHERE paymentId=?");
		
		mSelectAccountIdFromApikeyStmt = connection.prepareStatement("SELECT"
				+ " id FROM Account WHERE apikey=?");
		
		mInsertAccountStmt = connection.prepareStatement("INSERT INTO Account"
				+ " (username, apikey, passwordHash, passwordSalt)"
				+ " VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
		
		mSelectAccountInvoiceAmountSumStmt = connection.prepareStatement("SELECT SUM(amountSat)"
				+ " FROM Invoice WHERE accountId=? AND status=?");
		
		mSelectAccountWithdrawAmountSumStmt = connection.prepareStatement("SELECT SUM(amountSat)"
				+ " FROM Withdraw WHERE accountId=?");
		
		mInsertWithdrawStmt = connection.prepareStatement("INSERT INTO Withdraw"
				+ " (accountId, amountSat, date)"
				+ " VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
		
		mSelectQuoteStmt = connection.prepareStatement("SELECT"
				+ " bitcoinValue, lastUpdate"
				+ " FROM Quote WHERE currency=?");
		
		mUpdateQuoteStmt = connection.prepareStatement("INSERT INTO Quote"
				+ " (currency, bitcoinValue, lastUpdate)"
				+ " VALUES (?, ?, ?)"
				+ " ON DUPLICATE KEY UPDATE"
				+ " bitcoinValue=?, lastUpdate=?");
	}
}
