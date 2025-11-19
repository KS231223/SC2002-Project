package ims;

import common.*;
import cr.*;
import exceptions.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import staff.*;
import student.*;

/**
 * Registry used at login time to route users with different roles to the
 * matching home-page controller.
 */
public class LoginRegistry implements ControllerFactory {

    private final Map<String, ControllerSupplier> keyMap = new HashMap<>();

    /**
     * Configures the registry with handlers for each supported role.
     *
     * @param router  router managing controller navigation
     * @param scanner shared CLI scanner
     * @param store   backing entity store
     * @param userID  identifier of the authenticated user
     */
    public LoginRegistry(Router router, Scanner scanner, EntityStore store, String userID) {
        // Map roles to controller creation
        keyMap.put("student", () -> new StudentHomePageController(router, scanner, store, userID));
        keyMap.put("cr", () -> new CRHomePageController(router, scanner, store, userID));
        keyMap.put("staff", () -> new StaffHomePageController(router, scanner, store, userID));
    }




    @Override
    /**
     * Instantiates the home controller mapped to the provided role.
     *
     * @param role canonicalized user role (student/cr/staff)
     * @throws IllegalArgumentException when the role is unknown
     */
    public void createController(String role) {
        ControllerSupplier supplier = keyMap.get(role);
        if (supplier == null) {
            throw new IllegalArgumentException("No controller mapped for role: " + role);
        }

        try {
            supplier.create();
            // Controller constructor can auto-register with router
        } catch (InvalidStudentIDException | InvalidCompanyRepIDException | InvalidStaffIDException e) {
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    private interface ControllerSupplier {
        Controller create() throws InvalidStudentIDException, InvalidCompanyRepIDException, InvalidStaffIDException;
    }
}
