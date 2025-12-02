package br.com.toolschallenge.exception;

public class InvalidInstallmentsForPaymentTypeException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_MESSAGE =
            "Installments quantity must be lower than 2 when payment type is AVISTA";

    public InvalidInstallmentsForPaymentTypeException() {
        super(DEFAULT_MESSAGE);
    }
}