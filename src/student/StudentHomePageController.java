package student;

import common.*;
import exceptions.*;
import java.util.Scanner;

public class StudentHomePageController extends StudentController {

    private Display studentDisplay;

    // Constructor
    public StudentHomePageController(Router router, Scanner scanner, String studentID) throws InvalidStudentIDException {
        super(router, scanner, studentID);
        this.studentDisplay = new StudentHomeDisplay(this);
        router.replace(this); // swaps into this controller
    }

    @Override
    public void initialize() {
        System.out.println("Student Home Page loaded successfully for " + name + "!");
        this.handleMenu();
    }

    private void handleMenu() {
        while (true) {
            studentDisplay.print_menu();
            String choice = studentDisplay.get_user_input();

            try {
                switch (choice) {
                    case "1" -> new ViewInternshipController(router, scanner, studentID);
                    case "2" -> new UpdateInternshipFiltersController(router, scanner, studentID);
                    case "3" -> handleClearFilters();
                    case "4" -> new ApplyInternshipController(router, scanner, studentID);
                    case "5" -> new ViewApplicationsController(router, scanner, studentID);
                    case "6" -> new WithdrawalRequestController(router, scanner, studentID);
                    case "7" -> new AcceptOfferController(router, scanner, studentID);
                    case "8" -> new PasswordChanger(router, scanner, userID); // run change password
                    case "9" -> {
                        System.out.println("Logging out...");
                        router.pop();
                        return; // exit the loop
                    }
                    default -> System.out.println("Invalid option. Try again.");
                }
            } catch (Exception e) {
                // Catch any checked or runtime exception
                System.out.println("An error occurred: " + e.getMessage());
                e.printStackTrace(); // optional: print full stack trace for debugging
            }
        }
    }

    // Private inner display class for student home page
    private static class StudentHomeDisplay extends Display {

        public StudentHomeDisplay(Controller owner) {
            super(owner);
        }

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
            System.out.println("9. Logout");
            System.out.print("Select an option: ");
        }
    }

    private void handleClearFilters() {
        try {
            StudentFilterService.clearFilters(studentID);
            System.out.println("Internship filters cleared. Listings will default to alphabetical order.");
        } catch (IllegalArgumentException ex) {
            System.out.println("Unable to clear filters: " + ex.getMessage());
        }
    }
}
