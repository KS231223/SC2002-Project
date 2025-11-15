package exceptions;

/**
 * Signals that a company representative identifier does not map to a record.
 */
public class InvalidCompanyRepIDException extends Exception {
    /**
     * Creates the exception with a descriptive message.
     *
     * @param message detail explaining why the identifier is invalid
     */
    public InvalidCompanyRepIDException(String message) {
        super(message);
    }
}