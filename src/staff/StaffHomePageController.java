package staff;

import common.*;
import exceptions.*;
import ims.*;
import java.util.Scanner;

public class StaffHomePageController extends StaffController {

    private Display staffDisplay;
    // Constructor
    public StaffHomePageController(Router router, Scanner scanner, String staffID ) throws InvalidStaffIDException {
        super(router, scanner, staffID);
        this.staffDisplay = new StaffHomeDisplay(this);
        router.replace(this);
        //You access this by swapping roles into it so you use replace
    }



    @Override
    public void initialize() {
        System.out.println("Staff Home Page loaded successfully for " + name + "!");
        this.handleMenu();
    }
    private void handleMenu(){
        while (true) {
            staffDisplay.print_menu();
            String choice = staffDisplay.get_user_input();

            switch (choice) {
                case "1" -> new ReviewRegistrationController(router, scanner, staffID);
                case "2" -> new ReviewInternshipController(router, scanner, staffID);
                case "3" -> new ReviewWithdrawalController(router, scanner, staffID);
                case "4" -> System.out.println("Generate internship report – to be implemented.");
                case "5" -> new PasswordChanger(router, scanner, userID);
                case "6" -> { System.out.println("Logging out..."); router.pop(); return; }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

}


    // Private inner display class for staff home page


class StaffHomeDisplay extends Display {

    public StaffHomeDisplay(Controller owner) {
        super(owner);
    }

    @Override
    public void print_menu() {
        System.out.println("Welcome! Displaying staff dashboard...");
        System.out.println("=== Staff Home Page ===");
        System.out.println("=== Career Center Staff Menu ===");
        System.out.println("1. Review company representative registrations");
        System.out.println("2. Review internship submissions");
        System.out.println("3. Handle withdrawal requests");
        System.out.println("4. Generate internship report");
        System.out.println("5. Change password");
        System.out.println("6. Logout");
        System.out.print("Select an option: ");
    }
}