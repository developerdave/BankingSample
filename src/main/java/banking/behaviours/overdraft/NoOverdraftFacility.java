package banking.behaviours.overdraft;

/**
 * Overdraft behaviour which does not allow an overdraft
 */
public class NoOverdraftFacility implements IOverdraftBehavior {

	@Override
	public void deductFine(double withdrawalAmount) {
		// No op
	}

	@Override
	public boolean hasOverdraftFacility() {
		return false;
	}

	@Override
	public void withdraw(double value) {
		throw new IllegalArgumentException();
	}

	@Override
	public double getBalance() {
		return 0;
	}

	@Override
	public double deposit(double value) {
		return 0;
	}
}
