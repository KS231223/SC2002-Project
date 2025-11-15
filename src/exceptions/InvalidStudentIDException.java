package exceptions;

/**
 * Signals that a student identifier is not present in the student registry.
 */
public class InvalidStudentIDException extends Exception {
    /**
     * Creates the exception with context about the lookup failure.
     *
     * @param message detail explaining the invalid student identifier
     */
    public InvalidStudentIDException(String message) {
        super(message);
    }
}
