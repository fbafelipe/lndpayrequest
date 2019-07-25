package com.fbafelipe.lndpayrequest.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fbafelipe.lndpayrequest.di.ModuleFactory;
import com.fbafelipe.lndpayrequest.domain.model.Account;
import com.fbafelipe.lndpayrequest.domain.model.Invoice;
import com.fbafelipe.lndpayrequest.domain.model.Withdraw;
import com.fbafelipe.lndpayrequest.testutils.TestEnv;

public class DatabaseTest {
	private Database mDatabase;
	
	@Before
	public void setUp() throws Exception {
		TestEnv.getInstance().resetDatabase();
		
		ModuleFactory moduleFactory = new ModuleFactory();
		mDatabase = moduleFactory.getDatabase();
	}
	
	@After
	public void tearDown() throws Exception {
		TestEnv.getInstance().resetDatabase();
	}
	
	@Test
	public void testAccount() throws Exception {
		Account account = new Account();
		account.id = -1;
		account.apikey = "aaa";
		account.username = "testuser";
		account.passwordHash = "<topsceret>";
		account.passwordSalt = "salt";
		
		mDatabase.insertAccount(account);
		Long id = mDatabase.selectAccountIdFromApikey("aaa");
		
		assertNotEquals(-1, account.id);
		assertNotNull(id);
		assertEquals(account.id, id.longValue());
	}
	
	@Test
	public void testInvoice() throws Exception {
		Account account = createAndInsertAccount("testuser", "aaa");
		
		Invoice invoice = new Invoice();
		invoice.paymentId = "aaa";
		invoice.accountId = account.id;
		invoice.paymentRequest = "lndaaabbbccc";
		invoice.rHash = "bbb";
		invoice.amountSat = 1000L;
		invoice.date = 1000000L;
		invoice.paid = false;
		
		mDatabase.insertInvoice(invoice);
		Invoice result = mDatabase.selectInvoice("aaa");
		
		assertEquals("aaa", result.paymentId);
		assertEquals(1, result.accountId);
		assertEquals("bbb", result.rHash);
		assertEquals(1000L, result.amountSat);
		assertEquals(1000000L, result.date);
		assertEquals(false, result.paid);
		
		invoice.paid = true;
		
		mDatabase.updatePaidInvoice(invoice);
		result = mDatabase.selectInvoice("aaa");
		
		assertEquals("aaa", result.paymentId);
		assertEquals(account.id, result.accountId);
		assertEquals("bbb", result.rHash);
		assertEquals(1000L, result.amountSat);
		assertEquals(1000000L, result.date);
		assertEquals(true, result.paid);
	}
	
	@Test
	public void testInvoiceAmountSum() throws Exception {
		Account account1 = createAndInsertAccount("testuser", "aaa");
		Account account2 = createAndInsertAccount("testuser2", "aaa2");
		Account account3 = createAndInsertAccount("testuser3", "aaa3");
		
		createAndInsertInvoice(account1, 1, true);
		createAndInsertInvoice(account1, 2, true);
		Invoice invoice3 = createAndInsertInvoice(account1, 4, false);
		createAndInsertInvoice(account1, 8, false);
		
		createAndInsertInvoice(account2, 16, false);
		
		long account1AmountSum = mDatabase.selectAccountInvoiceAmountSum(account1.id);
		long account2AmountSum = mDatabase.selectAccountInvoiceAmountSum(account2.id);
		long account3AmountSum = mDatabase.selectAccountInvoiceAmountSum(account3.id);
		
		assertEquals(3, account1AmountSum);
		assertEquals(0, account2AmountSum);
		assertEquals(0, account3AmountSum);
		
		invoice3.paid = true;
		mDatabase.updatePaidInvoice(invoice3);
		
		account1AmountSum = mDatabase.selectAccountInvoiceAmountSum(account1.id);
		account2AmountSum = mDatabase.selectAccountInvoiceAmountSum(account2.id);
		account3AmountSum = mDatabase.selectAccountInvoiceAmountSum(account3.id);
		
		assertEquals(7, account1AmountSum);
		assertEquals(0, account2AmountSum);
		assertEquals(0, account3AmountSum);
	}
	
	@Test
	public void testWithdraw() throws Exception {
		Account account1 = createAndInsertAccount("testuser", "aaa");
		Account account2 = createAndInsertAccount("testuser2", "aaa2");
		Account account3 = createAndInsertAccount("testuser3", "aaa3");
		
		createAndInsertWithdraw(account1, 1);
		createAndInsertWithdraw(account1, 2);
		createAndInsertWithdraw(account1, 4);
		
		createAndInsertWithdraw(account2, 8);
		
		long account1WithdrawSum = mDatabase.selectAccountWithdrawAmountSum(account1.id);
		long account2WithdrawSum = mDatabase.selectAccountWithdrawAmountSum(account2.id);
		long account3WithdrawSum = mDatabase.selectAccountWithdrawAmountSum(account3.id);
		
		assertEquals(7, account1WithdrawSum);
		assertEquals(8, account2WithdrawSum);
		assertEquals(0, account3WithdrawSum);
	}
	
	private Account createAndInsertAccount(String userName, String apiKey) throws Exception {
		Account account = new Account();
		account.username = userName;
		account.apikey = apiKey;
		account.passwordHash = "<topsceret>";
		account.passwordSalt = "salt";
		
		mDatabase.insertAccount(account);
		
		return account;
	}
	
	private Invoice createAndInsertInvoice(Account account, long amount, boolean paid) throws Exception {
		Invoice invoice = new Invoice();
		invoice.paymentId = UUID.randomUUID().toString();
		invoice.accountId = account.id;
		invoice.paymentRequest = "lndaaabbbccc";
		invoice.rHash = UUID.randomUUID().toString();
		invoice.amountSat = amount;
		invoice.date = System.currentTimeMillis();
		invoice.paid = paid;
		
		mDatabase.insertInvoice(invoice);
		
		return invoice;
	}
	
	private Withdraw createAndInsertWithdraw(Account account, long amount) throws Exception {
		Withdraw withdraw = new Withdraw();
		withdraw.accountId = account.id;
		withdraw.amountSat = amount;
		withdraw.date = 1560000000000L;
		
		mDatabase.insertWithdraw(withdraw);
		
		return withdraw;
	}
}
