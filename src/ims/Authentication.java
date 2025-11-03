package ims;
import common.*;
import cr.*;
import staff.*;
import student.*;
import exceptions.*;
import java.util.Scanner;

public class Authentication extends Controller {

   private AuthenticationDisplay authenticationDisplay;

    public Authentication(Router router, Scanner scanner) {

        super(router,scanner);
        this.authenticationDisplay = new AuthenticationDisplay(this);
        router.push(this);
    }

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
    private boolean route_to(String chosen_route) {
        switch (chosen_route.trim()) {
            case "1": // Login
                System.out.println("\nRouting to Login...\n");
                new LoginController(router, scanner);
                return true;

            case "2": // Registration
                System.out.println("\nRouting to Register as Company Representative...\n");
                new RegistrationController(router, scanner);
                return true;

            case "3": // Exit
                System.out.println("Exiting system...");
                router.pop();
                return true;

            default: // Invalid option
                System.out.println("Invalid choice, please try again.\n");
                return false;
        }
    }
}


class AuthenticationDisplay extends Display {

    public AuthenticationDisplay(Controller owner) {
        super(owner); // call abstract class constructor
    }


    public void print_menu() {
        System.out.println("=== Internship Placement Management System ===");
        System.out.println("1. Login");
        System.out.println("2. Register as Company Representative");
        System.out.println("3. Exit");
        System.out.print("Enter your choice: ");
    }



}