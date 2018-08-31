package banking;

public class Payee {
	private String sortCode;
	private String accountNumber;
	private String payee;

	public Payee(String sortCode, String accountNumber, String payee) {
		this.sortCode = sortCode;
		this.accountNumber = accountNumber;
		this.payee = payee;
	}

	public String getPayee() {
		return payee;
	}
}
