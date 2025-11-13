package ims;
import common.*;
import java.util.*;

/**
 * Collects company representative registration details and records them for staff approval.
 */
public class RegistrationController extends Controller {
    Display registrationDisplay;
    private static final String PENDING_CR_FILE =
        PathResolver.resource("pending_cr.csv");

    /**
     * Creates a registration controller and immediately pushes it onto the router stack.
     *
     * @param router  shared navigation stack
     * @param scanner input source used to gather registration details
     */
    public RegistrationController(Router router, Scanner scanner) {
        super(router, scanner);
        this.registrationDisplay = new RegistrationDisplay(this);
        router.push(this);
    }

    /**
     * Prompts for company representative details and stores them in the pending queue.
     */
    public void initialize() {
        try {
            RegistrationDisplay display = (RegistrationDisplay) registrationDisplay;

            // Collect input via display
            String companyEmail = display.getInput("Company email (will be your login ID): ");
            String fullName = display.getInput("Full name: ");
            String companyName = display.getInput("Company name: ");
            String department = display.getInput("Department: ");
            String position = display.getInput("Position: ");
            String password = display.getInput("Password: ");


            // Create CREntity (CRID is None until assigned)
            CREntity newCR = new CREntity(companyEmail, password, fullName, companyName, department, position, companyEmail);

            // Save to pending_cr.csv via DatabaseManager
            DatabaseManager.appendEntry(PENDING_CR_FILE, newCR);

            System.out.println("\nRegistration submitted successfully! Await approval.");
        } catch (Exception e) {
            System.err.println("Error during registration: " + e.getMessage());
        } finally {
            destroy();
        }
    }

    /** Returns control to the authentication menu. */
    private void destroy() {
        System.out.println("\nReturning to main menu...\n");
        router.pop();
    }
}

/**
 * Console helper that renders registration prompts and gathers user input.
 */
class RegistrationDisplay extends Display {

    /**
     * Creates the display helper bound to the registration controller.
     *
     * @param owner controller coordinating this display
     */
    public RegistrationDisplay(Controller owner) {
        super(owner);
    }

    /** Prints the registration menu header. */
    public void print_menu() {
        System.out.println("=== Company Representative Registration ===");
    }

    // Simple input collector
    /**
     * Reads a response from the console after printing the supplied prompt.
     *
     * @param prompt text displayed to the user before reading input
     * @return captured user input
     */
    public String getInput(String prompt) {
        System.out.print(prompt);
        return get_user_input();
    }
}
