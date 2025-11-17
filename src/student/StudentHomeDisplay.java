package student;

import common.Controller;
import common.Display;

/**
 * Display wrapper that renders the student dashboard menu.
 */
public class StudentHomeDisplay extends Display {

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
        System.out.println("9. Logout");
        System.out.print("Select an option: ");
    }
}
