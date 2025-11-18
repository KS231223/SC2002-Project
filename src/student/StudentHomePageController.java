package student;

import common.*;
import exceptions.*;
import java.util.Scanner;

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
        while (true) {
            studentDisplay.print_menu();
            String choice = studentDisplay.get_user_input();

            try {
                switch (choice) {
                    case "1" -> new ViewInternshipController(router, scanner, entityStore, studentID);
                    case "2" -> new UpdateInternshipFiltersController(router, scanner, entityStore, studentID);
                    case "3" -> handleClearFilters();
                    case "4" -> new ApplyInternshipController(router, scanner, studentID);
                    case "5" -> new ViewApplicationsController(router, scanner, studentID);
                    case "6" -> new WithdrawalRequestController(router, scanner, studentID);
                    case "7" -> new AcceptOfferController(router, scanner, studentID);
                    case "8" -> new PasswordChanger(router, scanner, userID); // run change password
                    case "9" -> new ViewBookmarkedInternshipsController(router, scanner, studentID);
                    case "10" -> new ViewApplicationHistoryController(router, scanner, studentID);
                    case "11" -> {
                        System.out.println("Logging out...");
                        router.pop();
                        return; // exit the loop
                    }
                    default -> System.out.println("Invalid option. Try again.");
                }
            } catch (InvalidStudentIDException ex) {
                System.out.println("An error occurred: " + ex.getMessage());
            } catch (RuntimeException ex) {
                System.out.println("An unexpected error occurred: " + ex.getMessage());
            }
        }
    }

    // Private inner display class for student home page
    /**
     * Display wrapper that renders the student dashboard menu.
     */
    private static class StudentHomeDisplay extends Display {

        /**
         * Creates a display instance bound to the student home controller.
         *
         * @param owner parent controller
         */
        public StudentHomeDisplay(Controller owner) {
            super(owner);
        }

        /**
         * Prints the student dashboard menu options.
         */
        @Override
        public void print_menu() {
            System.out.println("=== Student Menu ===");
            System.out.println("1. View available internships");
            System.out.println("2. Update internship filters");
            System.out.println("3. Clear internship filters");
            System.out.println("4. Apply for an internship");
            System.out.println("5. View my applications");
            System.out.println("6. Request withdrawal");
            System.out.println("7. Accept internship offer");
            System.out.println("8. Change password");
            System.out.println("9. View bookmarked internships");
            System.out.println("10. View application history");
            System.out.println("11. Logout");
            System.out.print("Select an option: ");
        }
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
