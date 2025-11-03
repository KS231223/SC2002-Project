package exceptions;

// Custom exception for invalid company rep IDs
public class InvalidCompanyRepIDException extends Exception {
    public InvalidCompanyRepIDException(String message) {
        super(message);
    }
}