package banking;

import banking.behaviours.overdraft.OverdraftFacility;
import banking.services.AccountStore;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.fail;

public class CurrentAccountTest {
	private static final String USERNAME = "dave.green";
	private static final String PASSWORD = "secret1234";
	private Account.Credentials credentials;

	@Before
	public void setup() {
		AccountStore.deleteAll();
		credentials = new Account.Credentials(USERNAME, PASSWORD);
	}

	@Test
	public void whenOpenCurrentAccount_ThenCurrentAccountOpened() {
		// When
		Account account = Account.open(credentials, Account.AccountType.CURRENT);

		// Then
		assertThat(account.getAccountType()).isEqualTo(Account.AccountType.CURRENT);
	}

	@Test
	public void whenOpenAccount_ThenCurrentAccountTypeIsDefaulted() {
		// When
		Account account = Account.open(credentials);

		// Then
		assertThat(account.getAccountType()).isEqualTo(Account.AccountType.CURRENT);
	}

	@Test
	public void whenOpenAccount_ThenHasOverdraftFacility() {
		// When
		Account account = Account.open(credentials, Account.AccountType.CURRENT);

		// Then
		assertThat(account.getOverdraft().hasOverdraftFacility()).isEqualTo(true);
	}

	@Test
	public void whenOverdraftIsUsed_ThenFineIsApplied() throws InsufficientFundsException {
		// Given
		Account account = Account.open(credentials, Account.AccountType.CURRENT);

		// When
		account.withdraw(100D);

		// Then
		assertThat(account.getBalance()).isEqualTo(-100D - 100D * OverdraftFacility.FINE_PERCENTAGE);
	}

	@Test
	public void whenDepositIsMade_ThenOverdraftBalanceIsIncreased() throws InsufficientFundsException {
		// Given
		double withdrawalAmount = 100D;
		double depositAmount = 50D;

		Account account = Account.open(credentials, Account.AccountType.CURRENT);
		account.withdraw(withdrawalAmount);
		account.deposit(depositAmount);
		double fineAmount = withdrawalAmount * OverdraftFacility.FINE_PERCENTAGE;

		// Then
		assertThat(account.getOverdraft().getBalance()).isEqualTo(((withdrawalAmount * -1) + depositAmount) - fineAmount);
	}

	@Test
	public void whenWithdrawalIsMade_ThenNotPermittedToExceedOverdraftLimit()  {
		// Given
		double withdrawalAmount = 600D;

		Account account = Account.open(credentials, Account.AccountType.CURRENT);


		try {
			account.withdraw(withdrawalAmount);
			fail("Excepted InsufficientFundsException exception");
		} catch (InsufficientFundsException e) {
			e.printStackTrace();
		}
	}
}
