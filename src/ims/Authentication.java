package ims;

import common.Controller;
import common.EntityStore;
import common.Router;
import java.util.Scanner;

/**
 * Handles the top-level authentication choices for the CLI, allowing users to
 * log in, register as a company representative, or exit the system.
 */
public class Authentication extends Controller {

    private final AuthenticationDisplay authenticationDisplay;

    /**
     * Creates the authentication controller and binds it to the shared router and scanner.
     *
     * @param router  navigation stack used across the CLI
     * @param scanner shared input source for user prompts
     */
    public Authentication(Router router, Scanner scanner, EntityStore entityStore) {

        super(router, scanner, entityStore);
        this.authenticationDisplay = new AuthenticationDisplay(this);
    }

    /**
     * Starts the authentication flow by pushing this controller onto the router stack.
     */
    public void start() {
        router.push(this);
    }

    @Override
    public void initialize() {
        boolean validChoice = false;

        while (!validChoice) {
            try {
                // Show menu
                authenticationDisplay.print_menu();

                // Read user input
                String inputValue = authenticationDisplay.get_user_input();

                // Try to route
                validChoice = route_to(inputValue);

            } catch (Exception e) {
                System.err.println("An error occurred: " + e.getMessage());
                System.out.println("Please try again.\n");
            }
        }
    }

    /**
     * Routes the user to the controller that corresponds to the provided menu option.
     *
     * @param chosen_route raw menu option selected by the user
     * @return {@code true} when the option is recognised and handled, otherwise {@code false}
     */
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    private boolean route_to(String chosen_route) {
        return switch (chosen_route.trim()) {
            case "1" -> {
                System.out.println("\nRouting to Login...\n");
                new LoginController(router, scanner, entityStore).start();
                yield true;
            }
            case "2" -> {
                System.out.println("\nRouting to Register as Company Representative...\n");
                new RegistrationController(router, scanner, entityStore);
                yield true;
            }
            case "3" -> {
                System.out.println("Exiting system...");
                router.pop();
                yield true;
            }
            default -> {
                System.out.println("Invalid choice, please try again.\n");
                yield false;
            }
        };
    }
}


// Authentication display moved to `ims.AuthenticationDisplay`