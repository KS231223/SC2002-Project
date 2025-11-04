package staff;

import common.*;
import java.util.*;

public class ReviewInternshipController extends Controller {
    ReviewInternshipDisplay display;
    private static final String PENDING_INTERNSHIP_FILE =
        PathResolver.resource("pending_internship_opportunities.csv");
    private static final String INTERNSHIP_FILE =
        PathResolver.resource("internship_opportunities.csv");

    public ReviewInternshipController(Router router, Scanner scanner, String staffID) {
        super(router, scanner);
        display = new ReviewInternshipDisplay(this);
        router.push(this);
    }

    public void initialize() {
    List<Entity> pending = DatabaseManager.getDatabase(PENDING_INTERNSHIP_FILE, new ArrayList<>(), "Internship");
        if (pending.isEmpty()) {
            System.out.println("No pending internships.");
            router.pop();
            return;
        }
        display.print_menu();
        display.print_list(pending);
        String internshipId = display.get_user_input().trim();
    Entity internshipEntity = DatabaseManager.getEntryById(PENDING_INTERNSHIP_FILE, internshipId, "Internship");

        if (internshipEntity == null) {
            System.out.println("Invalid ID. Returning to previous menu.");
            router.pop();
            return;
        }

        display.print_entry(internshipEntity);
        String choice = display.get_user_input().trim().toUpperCase();

        if (choice.equals("A")) DatabaseManager.appendEntry(INTERNSHIP_FILE, internshipEntity);
        if (choice.equals("A") || choice.equals("R"))
            DatabaseManager.deleteEntry(PENDING_INTERNSHIP_FILE, internshipId, "Internship");

        System.out.println("\nReview complete.");
        router.pop();
    }
}

class ReviewInternshipDisplay extends Display {
    public ReviewInternshipDisplay(Controller owner) { super(owner); }

    @Override
    public void print_menu() {
            System.out.println("Welcome! Choose Internship ID to Approve/Reject");

    }

    public void print_entry(Entity e) {
        System.out.println("\nPending internship: " + e);
        System.out.print("Approve (A) / Reject (R): ");
    }

    public void print_list(List<Entity> list) {
        list.forEach(System.out::println);
        System.out.print("Enter Internship ID to approve/reject: ");
    }
}
