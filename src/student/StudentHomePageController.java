package student;

import common.*;
import exceptions.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Entry point for the student dashboard, providing navigation to key actions
 * such as viewing internships and managing applications.
 */
public class StudentHomePageController extends StudentController {

    private final Display studentDisplay;
    private ControllerFactory controllerFactory;

    // Constructor
    @SuppressWarnings("LeakingThisInConstructor")
    public StudentHomePageController(Router router, Scanner scanner, EntityStore entityStore, String studentID) throws InvalidStudentIDException {
        super(router, scanner, entityStore, studentID);
        this.studentDisplay = new StudentHomeDisplay(this);
        this.controllerFactory = createControllerFactory();
        router.replace(this); // swap into this controller
    }

    protected ControllerFactory createControllerFactory() {
        return new StudentHomeRegistry(router, scanner, entityStore, studentID);
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

            if ("3".equals(choice)) { // Clear filters special case
                handleClearFilters();
                continue;
            }
            if ("9".equals(choice)) { // Logout
                System.out.println("Logging out...");
                router.pop();
                return;
            }

            try {
                controllerFactory.createController(choice);
            } catch (Exception ex) {
                System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void handleClearFilters() {
        try {
            StudentFilterService.clearFilters(entityStore, studentID);
            System.out.println("Internship filters cleared. Listings will default to alphabetical order.");
        } catch (IllegalArgumentException ex) {
            System.out.println("Unable to clear filters: " + ex.getMessage());
        }
    }

    // Private inner display class

}
class StudentHomeDisplay extends Display {
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
