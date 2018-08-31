package banking.behaviours.interest;

/**
 * Interest behavior for an account, allows
 * different interest behaviours per account type
 */
public interface IInterestBehavior {
	double calculate(double balance);
}
