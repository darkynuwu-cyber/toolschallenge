package br.com.toolschallenge.exception;

public class PaymentNotFoundException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PaymentNotFoundException(String id) {
        super("Transaction not found for id: " + id);
    }
}