package banking.behaviours.overdraft;

import banking.InsufficientFundsException;

/**
 * Overdraft behavior for an account, allows
 * different overdraft behaviours per account type
 */
public interface IOverdraftBehavior {
	void deductFine(double withdrawalAmount) throws InsufficientFundsException;

	boolean hasOverdraftFacility();

	void withdraw(double value) throws InsufficientFundsException;

	double getBalance();

	double deposit(double value);
}
