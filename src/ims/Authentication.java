package ims;

import common.Controller;
import common.Display;
import common.Router;
import java.util.Scanner;

public class Authentication extends Controller {

   private final AuthenticationDisplay authenticationDisplay;

    public Authentication(Router router, Scanner scanner) {

        super(router,scanner);
        this.authenticationDisplay = new AuthenticationDisplay(this);
    }

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
     * Routes user to the chosen controller.
     * Returns true if a valid option was selected, false otherwise.
     */
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    private boolean route_to(String chosen_route) {
        return switch (chosen_route.trim()) {
            case "1" -> {
                System.out.println("\nRouting to Login...\n");
                new LoginController(router, scanner).start();
                yield true;
            }
            case "2" -> {
                System.out.println("\nRouting to Register as Company Representative...\n");
                new RegistrationController(router, scanner);
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


class AuthenticationDisplay extends Display {

    public AuthenticationDisplay(Controller owner) {
        super(owner); // call abstract class constructor
    }

    @Override
    public void print_menu() {
        System.out.println("=== Internship Placement Management System ===");
        System.out.println("1. Login");
        System.out.println("2. Register as Company Representative");
        System.out.println("3. Exit");
        System.out.print("Enter your choice: ");
    }



}