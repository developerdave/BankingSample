package banking.behaviours.interest;

/**
 * Interest behaviour supporting interest on an account.
 */
public class InterestFacility implements IInterestBehavior {
	public static final double INTEREST_PERCENTAGE = 1.003;

	@Override
	public double calculate(double balance) {
		return balance * INTEREST_PERCENTAGE;
	}
}
