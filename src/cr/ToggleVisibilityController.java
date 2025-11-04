package cr;

import common.*;
import exceptions.*;
import java.util.*;

public class ToggleVisibilityController extends CRController {

    private static final String INTERNSHIP_FILE =
        PathResolver.resource("internship_opportunities.csv");
    private ToggleVisibilityDisplay display;

    public ToggleVisibilityController(Router router, Scanner scanner, String crID) throws InvalidCompanyRepIDException {
        super(router, scanner, crID);
        this.display = new ToggleVisibilityDisplay(this);
        router.push(this);
    }

    @Override
    public void initialize() {
        try {
            List<Entity> allInternships = DatabaseManager.getDatabase(INTERNSHIP_FILE, new ArrayList<>(), "Internship");
            List<Entity> myInternships = new ArrayList<>();

            for (Entity e : allInternships) {
                if (e.getArrayValueByIndex(9).equals(userID)) {
                    myInternships.add(e);
                }
            }

            if (myInternships.isEmpty()) {
                System.out.println("No internships found for your account.");
                router.pop();
                return;
            }

            display.print_list(myInternships);
            String id = display.ask("Enter internship ID to toggle visibility: ");
            Entity internship = DatabaseManager.getEntryById(INTERNSHIP_FILE, id, "Internship");

            if (internship == null) {
                System.out.println("Invalid ID. Returning...");
                router.pop();
                return;
            }

            String current = internship.getArrayValueByIndex(11);
            internship.setArrayValueByIndex(11, current.equals("Visible") ? "Hidden" : "Visible");
            DatabaseManager.updateEntry(INTERNSHIP_FILE, id, internship, "Internship");
            System.out.println("Visibility toggled successfully!");

        } catch (Exception e) {
            System.err.println("Error toggling visibility: " + e.getMessage());
        } finally {
            router.pop();
        }
    }
}

class ToggleVisibilityDisplay extends Display {
    public ToggleVisibilityDisplay(Controller owner) {
        super(owner);
    }

    public void print_list(List<Entity> internships) {
        System.out.println("=== Your Internships ===");
        for (Entity e : internships) {
            System.out.println(e.toString());
        }
    }

    public String ask(String msg) {
        System.out.print(msg);
        return get_user_input();
    }

    @Override
    public void print_menu() {}
}
