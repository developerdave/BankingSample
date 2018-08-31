package banking.behaviours.interest;

/**
 * Interest behaviour on an account which does not pay interest.
 */
public class NoInterestFacility implements IInterestBehavior {
	@Override
	public double calculate(double balance) {
		return 0;
	}
}
