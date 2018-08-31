package banking;

import banking.services.AccountStore;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.fail;

public class AccountTest {
	private static final String USERNAME = "dave.green";
	private static final String PASSWORD = "secret1234";
	private Account.Credentials credentials;

	@Before
	public void setup() {
		AccountStore.deleteAll();
		credentials = new Account.Credentials(USERNAME, PASSWORD);
	}

	@Test
	public void whenOpenAccount_ThenAddedToAccountStore() {
		// When
		Account account = Account.open(credentials);

		// Then
		assertThat(AccountStore.getByUsername(credentials).size()).isEqualTo(1);
		assertThat(AccountStore.getByUsername(credentials).get(0).getAccountNumber()).isEqualTo(account.getAccountNumber());
	}

	@Test
	public void whenOpenNewAccount_ThenAccountCreated() {
		// When
		Account account = Account.open(credentials);

		// Then
		assertThat(account).isNotNull();
	}

	@Test
	public void whenOpenOpenMultipleAccounts_ThenAccountsCreated() {
		// Given
		Account.Credentials credentials1 = new Account.Credentials("steve.green", PASSWORD);

		// When
		Account account1 = Account.open(credentials);
		Account account2 = Account.open(credentials1);

		// Then
		assertThat(account1).isNotNull();
		assertThat(account2).isNotNull();
	}

	@Test
	public void whenDepositTenPoundToAccount_ThenBalanceIncreased() throws IllegalArgumentException {
		// Given
		Account account = Account.open(credentials);

		// When
		account.deposit(10D);

		// Then
		assertThat(account.getBalance()).isEqualTo(10D);
	}

	@Test
	public void whenMultipleDepositToAccount_ThenBalanceIncreased() throws IllegalArgumentException {
		// Given
		Account account = Account.open(credentials);

		// When
		account.deposit(20D);
		account.deposit(30.50D);

		// Then
		assertThat(account.getBalance()).isEqualTo(50.50D);
	}

	@Test
	public void whenDepositIsNegative_ThenReject() {
		// Given
		Account account = Account.open(credentials);

		// When
		try {
			account.deposit(-50D);
			fail("Expected IllegalArgumentException to be thrown");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage()).isEqualTo("Deposit value must be positive number");
		}
	}

	@Test
	public void whenWithdrawTenPoundFromAccount_ThenBalanceIsChanged() throws IllegalArgumentException, InsufficientFundsException {
		// Given
		Account account = Account.open(credentials);
		account.deposit(10D);

		// When
		account.withdraw(10D);

		// Then
		assertThat(account.getBalance()).isEqualTo(0);
	}

	@Test
	public void whenWithdrawalIsNegative_ThenReject() {
		// Given
		Account account = Account.open(credentials);
		account.deposit(10D);

		// When
		try {
			account.withdraw(-10D);
			fail("Expected IllegalArgumentException to be thrown");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage()).isEqualTo("Withdrawal amount must be a positive number");
		} catch (InsufficientFundsException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void whenRegisterPayee_ThenPayeeAdded() {
		// Given
		String sortCode = "001122";
		String accountNumber = "61247613";
		String payeeName = "eJavaGuru";
		Account account = Account.open(credentials);

		// When
		account.registerPayee(sortCode, accountNumber, payeeName);

		// Then
		assertThat(account.getPayees()).isNotNull();
		assertThat(account.getPayees().size()).isEqualTo(1);
	}

	@Test
	public void whenTransferFunds_ThenTransferCompleted() throws PayeeNotFoundException {
		// Given
		String sortCode = "001122";
		String accountNumber = "61247613";
		String payeeName = "eJavaGuru";
		Account account = Account.open(credentials);
		account.deposit(200D);
		account.registerPayee(sortCode, accountNumber, payeeName);

		// When
		account.makePayment(payeeName, 100D);

		// Then
		assertThat(account.getBalance()).isEqualTo(100D);
	}

	@Test
	public void whenTransferFundsToPayeeWhichIsNotRegistered_ThenExceptionThrown() {
		// Given
		String sortCode = "001122";
		String accountNumber = "61247613";
		String payeeName = "eJavaGuru";
		Account account = Account.open(credentials);
		account.deposit(200D);
		account.registerPayee(sortCode, accountNumber, payeeName);

		// When
		try {
			account.makePayment("dave", 200D);
			fail("Expected PayeeNotFoundException to be thrown");
		} catch (PayeeNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void whenCloseAccount_ThenStatusSetToClosed() {
		// Given
		Account account = Account.open(credentials);

		// When
		account.close();

		// Then
		assertThat(account.getStatus()).isEqualTo(Account.Status.CLOSED);
	}

	@Test
	public void whenCloseAccountAndHasBalance_ThenThrowException() {
		// Given
		Account account = Account.open(credentials);
		account.deposit(100D);

		// When
		try {
			account.close();
			fail("Expected IllegalStateException exception");
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void whenReopenAccount_ThenStatusSetToOpen() {
		// Given
		Account account = Account.open(credentials);
		account.close();

		// When
		account.reactivate();

		// Then
		assertThat(account.getStatus()).isEqualTo(Account.Status.OPEN);
	}
}
