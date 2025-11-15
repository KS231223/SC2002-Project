package exceptions;

/**
 * Signals that a generic user identifier is not associated with any account.
 */
public class InvalidUserIDException extends RuntimeException {
    /**
     * Creates the exception with a description of the lookup failure.
     *
     * @param message detail explaining the invalid user identifier
     */
    public InvalidUserIDException(String message) {
        super(message);
    }
}
