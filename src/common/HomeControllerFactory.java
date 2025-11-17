package common;

import exceptions.InvalidCompanyRepIDException;
import exceptions.InvalidStaffIDException;
import exceptions.InvalidStudentIDException;
import java.util.Scanner;

/**
 * Factory interface responsible for navigating to a user's home controller
 * based on their role. Implementations may perform navigation (push/replace)
 * as needed.
 */
public interface HomeControllerFactory {
    void navigateToHome(String role, Router router, Scanner scanner, EntityStore entityStore, String username)
        throws InvalidStaffIDException, InvalidStudentIDException, InvalidCompanyRepIDException;
}
