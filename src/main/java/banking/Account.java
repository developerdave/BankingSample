package banking;

import banking.behaviours.interest.IInterestBehavior;
import banking.behaviours.interest.InterestFacility;
import banking.behaviours.interest.NoInterestFacility;
import banking.behaviours.overdraft.IOverdraftBehavior;
import banking.behaviours.overdraft.NoOverdraftFacility;
import banking.behaviours.overdraft.OverdraftFacility;
import banking.services.AccountStore;
import sun.awt.geom.AreaOp;

import javax.security.auth.login.FailedLoginException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Bank account class, supports Savings and Current Accounts.
 */
public class Account {
	private Credentials credentials;
	private AccountType accountType;
	private IOverdraftBehavior overdraftBehavior;
	private IInterestBehavior interestBehavior;
	private UUID accountNumber;
	private double balance;
	private List<Payee> payees;
	private Status accountStatus;


	public enum AccountType {
		CURRENT,
		SAVINGS;

	}

	public enum Status {
		CLOSED,
		OPEN;

	}
	/**
	 * Create an account with credentials
	 *
	 * @param credentials Account credentials
	 */
	private Account(Credentials credentials, AccountType type) {
		accountType = type;    // Set account type
		accountNumber = UUID.randomUUID();    // Generate random account number
		balance = 0;    // Start the account off with a balance of zero
		payees = new ArrayList<>();    // Initialize payee list
		this.credentials = credentials;
	}
	/**
	 * Open a new account, defaults to a current account.
	 *
	 * @param credentials
	 * @return new account
	 */
	public static Account open(Credentials credentials) {
		return open(credentials, AccountType.CURRENT);    // By default open a current account
	}

	/**
	 * Open a new account
	 * NOTE: If the number of accounts increased past two then a Factory method would
	 * be more appropriate than the switch statement.
	 *
	 * @param credentials
	 * @param type account type to open
	 * @return new account
	 */
	public static Account open(Credentials credentials, AccountType type) {
		Account account = new Account(credentials, type);
		account.accountStatus = Status.OPEN;
		switch (type) {
			case CURRENT:
				// Current accounts have overdraft facility
				account.overdraftBehavior = new OverdraftFacility();
				account.interestBehavior = new NoInterestFacility();
				break;
			default:
				// Other accounts don't have an overdraft facility
				account.overdraftBehavior = new NoOverdraftFacility();
				account.interestBehavior = new InterestFacility();
		}

		AccountStore.add(account);

		return account;
	}

	/**
	 * Login to account using supplied credentials
	 *
	 * @param credentials
	 * @return login result
	 * @throws FailedLoginException
	 */
	public boolean login(Credentials credentials) throws FailedLoginException {
		if (!credentials.validate(credentials)) {
			throw new FailedLoginException();
		}
		return true;
	}

	/**
	 * Returns account type
	 *
	 * @return
	 */
	public AccountType getAccountType() {
		return accountType;
	}

	public Status getStatus() {
		return accountStatus;
	}

	public IOverdraftBehavior getOverdraft() {
		return overdraftBehavior;
	}

	public IInterestBehavior getInterest() { return interestBehavior; }

	/**
	 * Returns account credentials
	 *
	 * @return
	 */
	public Credentials getCredentials() {
		return credentials;
	}

	/**
	 * Returns the account number
	 *
	 * @return account number
	 */
	public UUID getAccountNumber() {
		return accountNumber;
	}

	/**
	 * Returns the current account balance
	 *
	 * @return account balance
	 */
	public double getBalance() {
		return balance + overdraftBehavior.getBalance();
	}

	/**
	 * NOTE: Doesn't account for multiple users and
	 * concurrent access to accounts
	 *
	 * @param value
	 */
	public void deposit(double value) throws IllegalArgumentException {
		if (value < 0) {
			throw new IllegalArgumentException("Deposit value must be positive number");
		}

		if (overdraftBehavior.getBalance() < 0) {
			double remaining = overdraftBehavior.deposit(value);
			balance += remaining;
		}
		balance += value;
	}

	/**
	 * Withdraw money from account
	 *
	 * @param value
	 * @throws IllegalArgumentException
	 */
	public void withdraw(double value) throws IllegalArgumentException, InsufficientFundsException {
		if (value < 0) {
			throw new IllegalArgumentException("Withdrawal amount must be a positive number");
		}

		if (balance <= 0) {
			// We need to use overdraft to withdraw from account
			if (overdraftBehavior.hasOverdraftFacility()) {
				overdraftBehavior.withdraw(value);
			} else {
				throw new InsufficientFundsException(String.format("Insufficient funds available account balance is %s", getBalance()));
			}
		} else {
			balance -= value;
		}
	}

	/**
	 * Register new payee
	 *
	 * @param sortCode
	 * @param accountNumber
	 * @param payeeName
	 */
	public void registerPayee(String sortCode, String accountNumber, String payeeName) {
		payees.add(new Payee(sortCode, accountNumber, payeeName));
	}

	/**
	 * Get list of payees on the account
	 *
	 * @return
	 */
	public List<Payee> getPayees() {
		return payees;
	}

	/**
	 * Make a payment from the account to a registered payee
	 *
	 * @param payeeName
	 * @param value
	 * @throws IllegalArgumentException
	 * @throws PayeeNotFoundException
	 */
	public void makePayment(String payeeName, double value) throws IllegalArgumentException, PayeeNotFoundException {
		if (value < 0) {
			throw new IllegalArgumentException("Payment amount must be a positive number");
		}
		// Find payee
		Optional<Payee> payee = payees
				.stream()
				.filter(f -> f.getPayee().equals(payeeName))
				.findFirst();

		if (payee.isPresent()) {    // Payee was found in the list with matching name
			// Make bank payment ...
			// ...

			balance -= value;
		} else {                    // Payee was not found in the list
			throw new PayeeNotFoundException(String.format("%s not found in list of payees", payeeName));
		}
	}

	public void calculateInterest() {
		balance += interestBehavior.calculate(balance);
	}

	public void close() {
		if (balance != 0) { throw new IllegalStateException("Balance not 0"); }

		accountStatus = Status.CLOSED;
	}

	public void reactivate() {
		accountStatus = Status.OPEN;
	}

	public static class Credentials {
		private String username;
		private String password;

		public Credentials(String username, String password) {
			this.username = username;
			this.password = generateHashedValue(password);    // Don't store raw password in object
		}

		public String getUsername() {
			return username;
		}

		/**
		 * Hashes the value using an MD5 MessageDigest
		 *
		 * @param value unhashed value
		 * @return hashed value
		 */
		private String generateHashedValue(String value) {
			String generatedHash = null;

			try {
				// Create MessageDigest instance for MD5
				MessageDigest messageDigest = MessageDigest.getInstance("MD5");

				// Add value to hash bytes to digest
				messageDigest.update(value.getBytes());

				// Get the hash's bytes
				byte[] bytes = messageDigest.digest();

				// This byte[] has bytes in decimal format, convert
				// to hexadecimal format
				StringBuilder sb = new StringBuilder();
				for (Byte b : bytes) {
					sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
				}

				// Get complete hashed password in hex format
				generatedHash = sb.toString();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}

			return generatedHash;
		}


		public boolean validate(Credentials credentials) {
			boolean result = false;
			if (username.equals(credentials.username) && password.equals(credentials.password)) {
				result = true;    // username & password matched
			}

			return result;
		}
	}
}
