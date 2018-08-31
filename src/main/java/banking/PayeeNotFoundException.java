package banking;

public class PayeeNotFoundException extends Exception {
	public PayeeNotFoundException(String message) {
		super(message);
	}
}
