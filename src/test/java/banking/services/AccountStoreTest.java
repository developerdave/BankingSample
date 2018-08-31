package banking.services;

import banking.Account;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class AccountStoreTest {
	private static final String USERNAME = "dave.green";
	private static final String PASSWORD = "secret1234";
	private Account.Credentials credentials;

	@Before
	public void setup() {
		credentials = new Account.Credentials(USERNAME, PASSWORD);
		AccountStore.deleteAll();
	}

	@Test
	public void whenGetByUsername_ThenReturnsAccount() {
		// Given
		Account.open(credentials);

		// When
		List<Account> accounts = AccountStore.getByUsername(credentials);

		// Then
		assertThat(accounts.size()).isEqualTo(1);
	}

	@Test
	public void whenGetByUsername_ThenCorrectAccountReturned() {
		// Given
		Account.open(credentials);

		Account.Credentials credentials1 = new Account.Credentials("steve.green", PASSWORD);
		Account.open(credentials1);

		// When
		List<Account> accounts = AccountStore.getByUsername(credentials);

		// Then
		List<Account> incorrectUsername = accounts.stream()
				.filter(f -> f.getCredentials().getUsername() != credentials.getUsername())	// Check for incorrect username
				.collect(Collectors.toList());
		assertThat(incorrectUsername.size()).isEqualTo(0);
	}

	@Test
	public void whenAddOneAccount_ThenAddedToAccountList() {
		// When
		Account.open(credentials);

		// Then
		assertThat(AccountStore.getByUsername(credentials).size()).isEqualTo(1);
	}

	@Test
	public void whenAddTwoAccounts_ThenAddedToAccountList() {
		// When
		Account.open(credentials);
		Account.open(credentials);

		// Then
		assertThat(AccountStore.getByUsername(credentials).size()).isEqualTo(2);
	}
}
