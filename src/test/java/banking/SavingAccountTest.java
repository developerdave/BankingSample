package banking;

import banking.behaviours.overdraft.OverdraftFacility;
import banking.services.AccountStore;
import org.assertj.core.api.Java6Assertions;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class SavingAccountTest {
	private static final String USERNAME = "dave.green";
	private static final String PASSWORD = "secret1234";
	private Account.Credentials credentials;
	private Account account;

	@Before
	public void setup() {
		AccountStore.deleteAll();
		credentials = new Account.Credentials(USERNAME, PASSWORD);
		account = Account.open(credentials, Account.AccountType.SAVINGS);
	}

	@Test
	public void whenOpenSavingsAccount_ThenSavingsAccountOpened() {
		// Then
		assertThat(account.getAccountType()).isEqualTo(Account.AccountType.SAVINGS);
	}

	@Test
	public void whenOpenAccount_ThenHasNoOverdraftFacility() {
		// Then
		assertThat(account.getOverdraft().hasOverdraftFacility()).isEqualTo(false);
	}

	@Test
	public void whenBalanceIsZero_ThenNoWithdrawalAllowed() {
		// When
		try {
			account.withdraw(100D);
			Java6Assertions.fail("Excepted InsufficientFundsException exception");
		} catch (InsufficientFundsException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void whenBalanceIsPositive_ThenInterestPaid() {
		// Given
		double depositAmount = 500D;
		account.deposit(depositAmount);

		// When
		account.calculateInterest();

		// Then
		assertThat(account.getBalance()).isGreaterThan(depositAmount);

	}
}
