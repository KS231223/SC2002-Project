package ims;

import common.*;
import cr.*;
import exceptions.*;
import staff.*;
import student.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class LoginRegistry implements ControllerFactory {

    private final Map<String, ControllerSupplier> keyMap = new HashMap<>();

    public LoginRegistry(Router router, Scanner scanner, EntityStore store, String userID) {
        // Map roles to controller creation
        keyMap.put("student", () -> new StudentHomePageController(router, scanner, store, userID));
        keyMap.put("cr", () -> new CRHomePageController(router, scanner, store, userID));
        keyMap.put("staff", () -> new StaffHomePageController(router, scanner, store, userID));
    }





    @Override
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
