package exceptions;

/**
 * Signals that a staff identifier cannot be resolved to a valid staff member.
 */
public class InvalidStaffIDException extends RuntimeException {
    /**
     * Creates the exception with a message describing the missing staff member.
     *
     * @param message detail explaining the invalid staff identifier
     */
    public InvalidStaffIDException(String message) {
        super(message);
    }
}
