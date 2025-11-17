package cr;

import common.*;
import exceptions.*;
import java.util.Scanner;

/**
 * Entry point for company representatives to manage internships and related
 * applications.
 */
public class CRHomePageController extends CRController {

    private final Display crDisplay;

    // Constructor

    /**
     * Builds the company representative home page controller and places it on
     * the router stack.
     *
     * @param router  router coordinating navigation
     * @param scanner shared console input
     * @param crID    identifier of the logged-in company representative
     * @throws InvalidCompanyRepIDException when {@code crID} cannot be resolved
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public CRHomePageController(Router router, Scanner scanner, EntityStore entityStore, String crID) throws InvalidCompanyRepIDException {
        super(router, scanner, entityStore, crID);
        this.crDisplay = new CRHomeDisplay(this);
        router.replace(this); // swap to this controller
    }

    /**
     * Announces successful login and launches the main menu loop.
     */
    @Override
    public void initialize() {
        System.out.println("Company Representative Home Page loaded successfully for " + name + "!");
        this.handleMenu();
    }

    /**
     * Processes menu interactions until the representative logs out.
     */
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    private void handleMenu() {
        while (true) {
            printActiveFiltersHeader();
            crDisplay.print_menu();
            String choice = crDisplay.get_user_input();
            try {
                switch (choice) {
                    case "1" -> new ListMyInternshipsController(router, scanner, entityStore, userID);
                    case "2" -> new CreateInternshipController(router, scanner, entityStore, userID);
                    case "3" -> new ToggleVisibilityController(router, scanner, entityStore, userID);
                    case "4" -> new ViewApplicationsController(router, scanner, entityStore, userID);
                    case "5" -> new ReviewApplicationController(router, scanner, entityStore, userID);
                    case "6" -> new FilterInternshipsController(router, scanner, entityStore, userID);
                    case "7" -> {
                        CRFilterService.clearFilters(userID);
                        System.out.println("Internship filters cleared.");
                    }
                    case "8" -> new PasswordChanger(router, scanner, entityStore, userID); // run change password
                    case "9" -> {
                        System.out.println("Logging out...");
                        router.pop();
                        return;
                    }
                    default -> System.out.println("Invalid option. Try again.");
                }
            } catch (InvalidCompanyRepIDException ex) {
                System.out.println("Unable to open the requested page: " + ex.getMessage());
            }
        }
    }

    /**
     * Prints the current filter summary for quick reference.
     */
    private void printActiveFiltersHeader() {
        System.out.println("Active filters: " + CRFilterService.summarize(userID));
    }

    // Private inner display class for CR home page
    /**
     * Display wrapper that renders the company representative menu.
     */
}
class CRHomeDisplay extends Display {

        /**
         * Creates a display instance tied to the home page controller.
         *
         * @param owner owning controller
         */
        public CRHomeDisplay(Controller owner) {
            super(owner);
        }

    /**
     * Prints the menu options available to company representatives.
     */
    @Override
        public void print_menu() {
            System.out.println("=== Company Representative Menu ===");
            System.out.println("1. List my internships");
            System.out.println("2. Create internship");
            System.out.println("3. Toggle internship visibility");
            System.out.println("4. View applications for my internships");
            System.out.println("5. Review internship application");
            System.out.println("6. Update internship filters");
            System.out.println("7. Clear internship filters");
            System.out.println("8. Change password");
            System.out.println("9. Logout");
            System.out.print("Select an option: \n");
        }
}

