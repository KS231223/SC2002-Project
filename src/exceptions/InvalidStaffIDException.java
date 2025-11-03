package exceptions;

public class InvalidStaffIDException extends RuntimeException {
    public InvalidStaffIDException(String message) {
        super(message);
    }
}
