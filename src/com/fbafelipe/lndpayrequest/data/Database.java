package com.fbafelipe.lndpayrequest.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.fbafelipe.lndpayrequest.domain.model.Invoice;
import com.fbafelipe.lndpayrequest.domain.model.User;
import com.fbafelipe.lndpayrequest.domain.model.Withdraw;
import com.fbafelipe.lndpayrequest.util.Utils;

public class Database {
	private ServerConfig mServerConfig;
	
	private Connection mConnection;
	
	private boolean mTablesCreated = false;
	
	private PreparedStatement mSelectInvoiceStmt;
	private PreparedStatement mInsertInvoiceStmt;
	private PreparedStatement mUpdateInvoicePaidStmt;
	
	private PreparedStatement mSelectUserIdFromApikeyStmt;
	private PreparedStatement mInsertUserStmt;
	
	private PreparedStatement mSelectUserInvoiceAmountSumStmt;
	private PreparedStatement mSelectUserWithdrawAmountSumStmt;
	private PreparedStatement mInsertUserWithdrawStmt;
	
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
				invoice.userId = resultSet.getLong(1);
				invoice.rHash = resultSet.getString(2);
				invoice.paymentRequest = resultSet.getString(3);
				invoice.amountSat = resultSet.getLong(4);
				invoice.date = resultSet.getLong(5);
				invoice.paid = resultSet.getBoolean(6);
				return invoice;
			}
			
			return null;
		}
	}
	
	public synchronized void insertInvoice(Invoice invoice) throws SQLException {
		getConnection();
		mInsertInvoiceStmt.setString(1, invoice.paymentId);
		mInsertInvoiceStmt.setLong(2, invoice.userId);
		mInsertInvoiceStmt.setString(3, invoice.rHash);
		mInsertInvoiceStmt.setString(4, invoice.paymentRequest);
		mInsertInvoiceStmt.setLong(5, invoice.amountSat);
		mInsertInvoiceStmt.setLong(6, invoice.date);
		mInsertInvoiceStmt.setBoolean(7, invoice.paid);
		
		mInsertInvoiceStmt.execute();
		
		// just to force a throw if somehow the insert failed
		getGeneratedKey(mInsertInvoiceStmt);
	}
	
	public synchronized void updatePaidInvoice(Invoice invoice) throws SQLException {
		getConnection();
		mUpdateInvoicePaidStmt.setBoolean(1, invoice.paid);
		mUpdateInvoicePaidStmt.setString(2, invoice.paymentId);
		
		mUpdateInvoicePaidStmt.execute();
	}
	
	public synchronized Long selectUserIdFromApikey(String apikey) throws SQLException {
		getConnection();
		mSelectUserIdFromApikeyStmt.setString(1, apikey);
		mSelectUserIdFromApikeyStmt.execute();
		
		try (ResultSet resultSet = mSelectUserIdFromApikeyStmt.getResultSet()) {
			if (resultSet.next()) {
				return resultSet.getLong(1);
			}
			
			return null;
		}
	}
	
	public synchronized void insertUser(User user) throws SQLException {
		getConnection();
		mInsertUserStmt.setString(1, user.username);
		mInsertUserStmt.setString(2, user.apikey);
		mInsertUserStmt.setString(3, user.passwordHash);
		mInsertUserStmt.setString(4, user.passwordSalt);
		mInsertUserStmt.execute();
		
		user.id = getGeneratedKey(mInsertUserStmt);
	}
	
	public synchronized Long selectUserInvoiceAmountSum(long userId) throws SQLException {
		getConnection();
		mSelectUserInvoiceAmountSumStmt.setLong(1, userId);
		mSelectUserInvoiceAmountSumStmt.execute();
		
		try (ResultSet resultSet = mSelectUserInvoiceAmountSumStmt.getResultSet()) {
			if (resultSet.next()) {
				return resultSet.getLong(1);
			}
			
			return null;
		}
	}
	
	public synchronized Long selectUserWithdrawAmountSum(long userId) throws SQLException {
		getConnection();
		mSelectUserWithdrawAmountSumStmt.setLong(1, userId);
		mSelectUserWithdrawAmountSumStmt.execute();
		
		try (ResultSet resultSet = mSelectUserWithdrawAmountSumStmt.getResultSet()) {
			if (resultSet.next()) {
				return resultSet.getLong(1);
			}
			
			return null;
		}
	}
	
	public synchronized void insertUserWithdraw(Withdraw withdraw) throws SQLException {
		getConnection();
		mInsertUserWithdrawStmt.setLong(1, withdraw.userId);
		mInsertUserWithdrawStmt.setLong(2, withdraw.amountSat);
		mInsertUserWithdrawStmt.setLong(3, withdraw.date);
		mInsertUserWithdrawStmt.execute();
		
		// just to force a throw if somehow the insert failed
		getGeneratedKey(mInsertUserWithdrawStmt);
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
		
		Statement statement = connection.createStatement();
		statement.execute(sql);
	}
	
	private void compileStatements(Connection connection) throws SQLException {
		mSelectInvoiceStmt = connection.prepareStatement("SELECT"
				+ " userId, rHash, paymentRequest, amountSat, date, paid"
				+ " FROM Invoice WHERE paymentId=?");
		
		mInsertInvoiceStmt = connection.prepareStatement("INSERT INTO Invoice"
				+ " (paymentId, userId, rHash, paymentRequest, amountSat, date, paid)"
				+ " VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
		
		mUpdateInvoicePaidStmt = connection.prepareStatement("UPDATE Invoice"
				+ " SET paid=? WHERE paymentId=?");
		
		mSelectUserIdFromApikeyStmt = connection.prepareStatement("SELECT"
				+ " id FROM User WHERE apikey=?");
		
		mInsertUserStmt = connection.prepareStatement("INSERT INTO User"
				+ " (username, apikey, passwordHash, passwordSalt)"
				+ " VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
		
		mSelectUserInvoiceAmountSumStmt = connection.prepareStatement("SELECT SUM(amountSat)"
				+ " FROM Invoice WHERE userId=?");
		
		mSelectUserWithdrawAmountSumStmt = connection.prepareStatement("SELECT SUM(amountSat)"
				+ " FROM Withdraw WHERE userId=?");
		
		mInsertUserWithdrawStmt = connection.prepareStatement("INSERT INTO Withdraw"
				+ " (userId, amountSat, date)"
				+ " VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
	}
}
