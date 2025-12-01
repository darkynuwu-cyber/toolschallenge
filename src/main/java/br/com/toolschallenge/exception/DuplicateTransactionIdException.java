package br.com.toolschallenge.exception;

public class DuplicateTransactionIdException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DuplicateTransactionIdException(String id) {
        super("Transaction with id '" + id + "' already exists");
    }
}