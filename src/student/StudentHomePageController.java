package student;

import common.*;
import exceptions.*;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Entry point for the student dashboard, providing navigation to key actions
 * such as viewing internships and managing applications.
 */
public class StudentHomePageController extends StudentController {

    private final StudentHomeDisplay studentDisplay;

    // Constructor
    @SuppressWarnings("LeakingThisInConstructor")
    public StudentHomePageController(Router router, Scanner scanner, EntityStore entityStore, String studentID) throws InvalidStudentIDException {
        super(router, scanner, entityStore, studentID);
        this.studentDisplay = new StudentHomeDisplay(this);
        router.replace(this); // swap into this controller
    }

    @Override
    public void initialize() {
        System.out.println("Student Home Page loaded successfully for " + name + "!");
        this.handleMenu();
    }

    private void handleMenu() {
        while (true) {
            studentDisplay.print_menu();
            if (!scanner.hasNextLine()) {
                System.out.println("Input stream closed. Logging out...");
                router.pop();
                return;
            }

            String choice = studentDisplay.get_user_input();

            try {
                switch (choice) {
                    case "1" -> new ViewInternshipController(router, scanner, entityStore, studentID);
                    case "2" -> new UpdateInternshipFiltersController(router, scanner, entityStore, studentID);
                    case "3" -> handleClearFilters();
                    case "4" -> new ApplyInternshipController(router, scanner, entityStore, studentID);
                    case "5" -> new ViewApplicationsController(router, scanner, entityStore, studentID);
                    case "6" -> new WithdrawalRequestController(router, scanner, entityStore, studentID);
                    case "7" -> new AcceptOfferController(router, scanner, entityStore, studentID);
                    case "8" -> new PasswordChanger(router, scanner, entityStore, userID); // run change password
                    case "9" -> new ViewBookmarkedInternshipsController(router, scanner, entityStore, studentID);
                    case "10" -> new ViewApplicationHistoryController(router, scanner, entityStore, studentID);
                    case "11" -> {
                        System.out.println("Logging out...");
                        router.pop();
                        return; // exit the loop
                    }
                    default -> System.out.println("Invalid option. Try again.");
                }
            } catch (InvalidStudentIDException ex) {
                System.out.println("An error occurred: " + ex.getMessage());
            } catch (NoSuchElementException | IllegalStateException eof) {
                System.out.println("Input stream closed. Logging out...");
                router.pop();
                return;
            } catch (RuntimeException ex) {
                System.out.println("An unexpected error occurred: " + ex.getMessage());
            }
        }
    }

    // Private inner display class
    private static final class StudentHomeDisplay extends Display {
        private StudentHomeDisplay(Controller owner) {
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
