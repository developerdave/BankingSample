package banking.services;

import banking.Account;

import javax.security.auth.login.FailedLoginException;
import java.util.*;

/**
 * Store of accounts
 */
public class AccountStore {
	private static HashMap<String, List<Account>> accounts = new HashMap<>();

	/**
	 * Get list of accounts by username. If login is unsuccessful the account is
	 * not returned in the account list.
	 * @param credentials
	 * @return
	 */
	public static List<Account> getByUsername(Account.Credentials credentials) {
		 List<Account> accountList = accounts.get(credentials.getUsername());
		 List<Account> results = new ArrayList<>();

		 for (Account a : accountList) {

			 try {
				 if (a.login(credentials)) {
				 	results.add(a);	// Add authenticated account to results list
				 }

			 } catch (FailedLoginException e) {
				 e.printStackTrace();
			 }
		 }

		 return results;
	}

	/**
	 * Add an account to the store.
	 * @param account
	 */
	public static void add(Account account) {
		List<Account> existing = accounts.get(account.getCredentials().getUsername());
		if (existing != null) {	// User already has an account
			existing.add(account);	// Add another account
		} else {	// User does not have any accounts
			existing = new ArrayList<>();
			existing.add(account);
			accounts.put(account.getCredentials().getUsername(), existing);	// Add the account to `HashMap`
		}
	}

	/**
	 * Delete all accounts in the store.
	 */
	public static void deleteAll() {
		accounts = new HashMap<>();
	}
}
