package banking.behaviours.overdraft;

import banking.InsufficientFundsException;

/**
 * Overdraft behaviour which allows the user to have an overdraft
 * on their account.
 */
public class OverdraftFacility implements IOverdraftBehavior {
	public static final double FINE_PERCENTAGE = 0.01;
	public static final double OVERDRAFT_LIMIT = 500D;

	private double balance = 0;

	@Override
	public void deductFine(double withdrawalAmount) throws InsufficientFundsException {
		if (withdrawalAmount * FINE_PERCENTAGE > OVERDRAFT_LIMIT) {
			throw new InsufficientFundsException(String.format("Withdrawal would exceed your overdraft limit"));
		}

		balance -= withdrawalAmount * FINE_PERCENTAGE;
	}

	@Override
	public boolean hasOverdraftFacility() {
		return true;
	}

	@Override
	public void withdraw(double value) throws InsufficientFundsException {
		deductFine(value);	// Fine account holder
		if (value > OVERDRAFT_LIMIT) throw new InsufficientFundsException(String.format("Withdrawal would exceed your overdraft limit"));

		balance -= value;
	}

	@Override
	public double getBalance() {
		return balance;
	}

	@Override
	public double deposit(double value) {
		if (Math.abs(balance) < value) {
			value = value - balance;	// Take the overdraft balance from the deposit
			balance = 0;	// Set balance to 0
			return value;	// Return the remainder
		} else {
			balance += value;	// Decrease the balance
		}

		return 0;	// Remainder is 0
	}
}
