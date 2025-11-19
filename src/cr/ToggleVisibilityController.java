package cr;

import common.*;
import exceptions.*;
import java.util.*;

/**
 * Controller that lets a company representative toggle an internship's public visibility.
 */
public class ToggleVisibilityController extends CRController {

    private static final String INTERNSHIP_FILE =
        PathResolver.resource("internship_opportunities.csv");
    private ToggleVisibilityDisplay display;

    /**
     * Creates the controller and registers it on the router stack.
     *
     * @param router   router used for lifecycle management
     * @param scanner  shared scanner for CLI prompts
     * @param entityStore persistence access
     * @param crID     active company representative identifier
     * @throws InvalidCompanyRepIDException when the CR ID fails validation
     */
    public ToggleVisibilityController(Router router, Scanner scanner, EntityStore entityStore, String crID) throws InvalidCompanyRepIDException {
        super(router, scanner, entityStore, crID);
        this.display = new ToggleVisibilityDisplay(this);
        router.push(this);
    }

    @Override
    public void initialize() {
        try {
            List<Entity> allInternships = entityStore.loadAll(INTERNSHIP_FILE, "Internship");
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
            Entity internship = entityStore.findById(INTERNSHIP_FILE, id, "Internship");

            if (internship == null) {
                System.out.println("Invalid ID. Returning...");
                router.pop();
                return;
            }

            String current = internship.getArrayValueByIndex(11);
            internship.setArrayValueByIndex(11, current.equals("Visible") ? "Hidden" : "Visible");
            entityStore.update(INTERNSHIP_FILE, id, internship, "Internship");
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

    /**
     * Prints the internships owned by the current CR.
     *
     * @param internships internships associated with the user
     */
    public void print_list(List<Entity> internships) {
        System.out.println("=== Your Internships ===");
        for (Entity e : internships) {
            System.out.println(e.toString());
        }
    }

    /**
     * Prompts the user for a value and returns the input.
     *
     * @param msg message displayed before reading input
     * @return raw user input
     */
    public String ask(String msg) {
        System.out.print(msg);
        return get_user_input();
    }

    @Override
    public void print_menu() {}
}
