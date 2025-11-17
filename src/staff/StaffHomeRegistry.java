package staff;

import common.*;
import exceptions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class StaffHomeRegistry implements ControllerFactory {

    private final Map<String, ControllerSupplier> keyMap = new HashMap<>();
    private final Router router;
    private final Scanner scanner;
    private final EntityStore store;
    private final String staffID;
    private final StaffReviewFilters filters;

    public StaffHomeRegistry(Router router, Scanner scanner, EntityStore store, String staffID, StaffReviewFilters filters) {
        this.router = router;
        this.scanner = scanner;
        this.store = store;
        this.staffID = staffID;
        this.filters = filters;

        keyMap.put("1", () -> new ReviewRegistrationController(router, scanner, store, staffID, filters));
        keyMap.put("2", () -> new ReviewInternshipController(router, scanner, store, staffID, filters));
        keyMap.put("3", () -> new ReviewWithdrawalController(router, scanner, store, staffID, filters));
        keyMap.put("6", () -> new InternshipReportController(router, scanner, store, staffID, filters));
        keyMap.put("7", () -> new PasswordChanger(router, scanner, store, staffID));
    }

    @Override
    public void createController(String key) {
        ControllerSupplier supplier = keyMap.get(key);
        if (supplier == null) {
            throw new IllegalArgumentException("No controller mapped for key: " + key);
        }

        try {
            supplier.create();
        } catch (InvalidStaffIDException e) {
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    private interface ControllerSupplier {
        Controller create() throws InvalidStaffIDException;
    }
}
