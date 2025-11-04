package cr;

import common.*;
import exceptions.InvalidCompanyRepIDException;
import java.util.*;

public class ReviewApplicationController extends CRController {
    private final ReviewApplicationDisplay display;
    private static final String APPLICATION_FILE =
        PathResolver.resource("internship_applications.csv");
    private static final String INTERNSHIP_FILE =
        PathResolver.resource("internship_opportunities.csv");

    public ReviewApplicationController(Router router, Scanner scanner, String crID) throws InvalidCompanyRepIDException {
        super(router, scanner, crID);
        this.display = new ReviewApplicationDisplay(this);
        router.push(this);
    }

    @Override
    public void initialize() {
        List<Entity> applications = DatabaseManager.getDatabase(APPLICATION_FILE, new ArrayList<>(), "Application");

        if (applications.isEmpty()) {
            System.out.println("No applications to review.");
            router.pop();
            return;
        }

        display.print_menu();
        display.print_list(applications);

        String applicationId = display.get_user_input().trim();
        Entity application = DatabaseManager.getEntryById(APPLICATION_FILE, applicationId, "Application");
        Entity internshipToCheck  = DatabaseManager.getEntryById(INTERNSHIP_FILE, application.getArrayValueByIndex(2), "Internship");
        if (application == null || !internshipToCheck.getArrayValueByIndex(9).equals(userID)) {
            System.out.println("Invalid Application ID. Returning to previous menu.");
            router.pop();
            return;
        }

        display.print_entry(application);
        String choice = display.get_user_input().trim().toUpperCase();

        try {
            if (choice.equals("A")) {
                ApplicationHandler.approveApplication(applicationId);

            }
            else if (choice.equals("R")) {
                application.setArrayValueByIndex(3, "Rejected");
                DatabaseManager.updateEntry(APPLICATION_FILE, applicationId, application, "Application");
                System.out.println("Application rejected successfully.");
            }
            else {
                System.out.println("Invalid choice. Returning...");
            }
        } catch (Exception e) {
            System.err.println("Error while reviewing application: " + e.getMessage());
        }

        System.out.println("\nReview complete.");
        router.pop();
    }
}

class ReviewApplicationDisplay extends Display {
    public ReviewApplicationDisplay(Controller owner) { super(owner); }

    @Override
    public void print_menu() {
        System.out.println("=== Review Internship Applications ===");
        System.out.println("Select Application ID to Approve/Reject");
    }

    public void print_entry(Entity e) {
        System.out.println("\nApplication Details: " + e);
        System.out.print("Approve (A) / Reject (R): ");
    }

    public void print_list(List<Entity> list) {
        for (Entity e : list) {
            System.out.println(e.toString());
        }
        System.out.print("Enter Application ID to review: ");
    }
}
