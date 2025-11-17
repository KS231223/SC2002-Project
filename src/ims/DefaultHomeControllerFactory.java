package ims;

import common.*;
import exceptions.*;
import student.*;
import staff.*;
import cr.*;
import java.util.Objects;
import java.util.Scanner;

/**
 * Default implementation used by the application to route users to their
 * appropriate home controller based on role.
 */
public class DefaultHomeControllerFactory implements HomeControllerFactory {

    @Override
    public void navigateToHome(String role, Router router, Scanner scanner, EntityStore entityStore, String username)
            throws InvalidStaffIDException, InvalidStudentIDException, InvalidCompanyRepIDException {
        if (role == null) role = "";
        role = role.trim().toLowerCase();

        switch (role) {
            case "staff" -> new StaffHomePageController(router, scanner, entityStore, username).open();
            case "student" -> new StudentHomePageController(router, scanner, entityStore, username);
            case "cr" -> new CRHomePageController(router, scanner, entityStore, username);
            default -> System.err.println("Unknown role: " + role);
        }
    }
}
