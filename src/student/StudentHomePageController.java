package student;

import common.*;
import exceptions.*;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

/**
 * Entry point for the student dashboard, providing navigation to key actions
 * such as viewing internships and managing applications.
 */
public class StudentHomePageController extends StudentController {

    private final Display studentDisplay;

    // Constructor
    /**
     * Builds the student home page controller and places it on the router.
     *
     * @param router    router responsible for navigation
     * @param scanner   shared console input
     * @param studentID identifier for the logged-in student
     * @throws InvalidStudentIDException when {@code studentID} is invalid
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public StudentHomePageController(Router router, Scanner scanner, EntityStore entityStore, String studentID) throws InvalidStudentIDException {
        super(router, scanner, entityStore, studentID);
        this.studentDisplay = new StudentHomeDisplay(this);
        router.replace(this); // swaps into this controller
    }

    /**
     * Announces the dashboard and begins processing menu interactions.
     */
    @Override
    public void initialize() {
        System.out.println("Student Home Page loaded successfully for " + name + "!");
        this.handleMenu();
    }

    /**
     * Runs the main menu loop until the student logs out.
     */
    @SuppressWarnings({"ResultOfObjectAllocationIgnored", "TooBroadCatch"})
    private void handleMenu() {
        Map<String, ControllerCreator> factory = buildFactory();

        while (true) {
            studentDisplay.print_menu();
            String choice = studentDisplay.get_user_input();

            // Handle non-controller actions first
            if ("3".equals(choice)) {
                handleClearFilters();
                continue;
            }

            if ("9".equals(choice)) {
                System.out.println("Logging out...");
                router.pop();
                return;
            }

            ControllerCreator creator = factory.get(choice);
            if (creator != null) {
                try {
                    creator.create();
                } catch (InvalidStudentIDException ex) {
                    System.out.println("An error occurred: " + ex.getMessage());
                } catch (RuntimeException ex) {
                    System.out.println("An unexpected error occurred: " + ex.getMessage());
                } catch (Exception ex) {
                    System.out.println("An error occurred: " + ex.getMessage());
                }
                continue;
            }

            System.out.println("Invalid option. Try again.");
        }
    }

    // Display is now provided by `StudentHomeDisplay` in its own file.

    private Map<String, ControllerCreator> buildFactory() {
        Map<String, ControllerCreator> m = new HashMap<>();
        m.put("1", () -> new ViewInternshipController(router, scanner, entityStore, studentID));
        m.put("2", () -> new UpdateInternshipFiltersController(router, scanner, entityStore, studentID));
        m.put("4", () -> new ApplyInternshipController(router, scanner, entityStore, studentID));
        m.put("5", () -> new ViewApplicationsController(router, scanner, entityStore, studentID));
        m.put("6", () -> new WithdrawalRequestController(router, scanner, entityStore, studentID));
        m.put("7", () -> new AcceptOfferController(router, scanner, entityStore, studentID));
        m.put("8", () -> new PasswordChanger(router, scanner, entityStore, userID));
        return m;
    }

    /**
     * Clears any saved internship filters for the current student.
     */
    private void handleClearFilters() {
        try {
            StudentFilterService.clearFilters(entityStore, studentID);
            System.out.println("Internship filters cleared. Listings will default to alphabetical order.");
        } catch (IllegalArgumentException ex) {
            System.out.println("Unable to clear filters: " + ex.getMessage());
        }
    }
}
