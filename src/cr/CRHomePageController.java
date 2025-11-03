package cr;

import common.*;
import exceptions.*;
import ims.*;
import java.util.Scanner;

public class CRHomePageController extends CRController {

    private Display crDisplay;

    // Constructor
    public CRHomePageController(Router router, Scanner scanner, String crID) throws InvalidCompanyRepIDException {
        super(router, scanner, crID);
        this.crDisplay = new CRHomeDisplay(this);
        router.replace(this); // swap to this controller
    }

    @Override
    public void initialize() {
        System.out.println("Company Representative Home Page loaded successfully for " + name + "!");
        this.handleMenu();
    }

    private void handleMenu() {
        while (true) {
            crDisplay.print_menu();
            String choice = crDisplay.get_user_input();
            try {
                switch (choice) {
                    case "1":
                        new ListMyInternshipsController(router, scanner, userID);
                        break;
                    case "2":
                        new CreateInternshipController(router, scanner, userID);
                        break;
                    case "3":
                        new ToggleVisibilityController(router, scanner, userID);
                        break;
                    case "4":
                        new ViewApplicationsController(router, scanner, userID);
                        break;
                    case "5":
                        new ReviewApplicationController(router, scanner, userID);
                        break;
                    case "6":
                        new PasswordChanger(router, scanner, userID); // run change password
                        break;
                    case "7":
                        System.out.println("Logging out...");
                        router.pop();
                        return;
                    default:
                        System.out.println("Invalid option. Try again.");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Private inner display class for CR home page
    private static class CRHomeDisplay extends Display {

        public CRHomeDisplay(Controller owner) {
            super(owner);
        }

        @Override
        public void print_menu() {
            System.out.println("=== Company Representative Menu ===");
            System.out.println("1. List my internships");
            System.out.println("2. Create internship");
            System.out.println("3. Toggle internship visibility");
            System.out.println("4. View applications for my internships");
            System.out.println("5. Review internship application");
            System.out.println("6. Change password");
            System.out.println("7. Logout");
            System.out.print("Select an option: ");
        }
    }
}
