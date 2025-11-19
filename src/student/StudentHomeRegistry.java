package student;

import common.*;
import exceptions.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Registry that maps student home-page menu selections to controller factories.
 */
public class StudentHomeRegistry implements ControllerFactory {

    private final Map<String, ControllerSupplier> keyMap = new HashMap<>();

    /**
     * Wires student actions to their corresponding controllers.
     *
     * @param router   router managing controller stack
     * @param scanner  shared CLI scanner
     * @param store    student-facing entity store
     * @param studentID current student identifier
     */
    public StudentHomeRegistry(Router router, Scanner scanner, EntityStore store, String studentID) {
        keyMap.put("1", () -> new ViewInternshipController(router, scanner, store, studentID));
        keyMap.put("2", () -> new UpdateInternshipFiltersController(router, scanner, store, studentID));
        keyMap.put("4", () -> new ApplyInternshipController(router, scanner, store, studentID));
        keyMap.put("5", () -> new ViewApplicationsController(router, scanner, store, studentID));
        keyMap.put("6", () -> new WithdrawalRequestController(router, scanner, store, studentID));
        keyMap.put("7", () -> new AcceptOfferController(router, scanner, store, studentID));
        keyMap.put("8", () -> new PasswordChanger(router, scanner, store, studentID));
    }

    @Override
    /**
     * Instantiates the controller associated with the supplied menu key.
     *
     * @param key student home option entered by the user
     * @throws IllegalArgumentException when the key has no mapping
     */
    public void createController(String key) {
        ControllerSupplier supplier = keyMap.get(key);
        if (supplier == null) {
            throw new IllegalArgumentException("No controller mapped for key: " + key);
        }
        try {
            supplier.create();
        } catch (InvalidStudentIDException e) {
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    private interface ControllerSupplier {
        Controller create() throws InvalidStudentIDException;
    }
}
