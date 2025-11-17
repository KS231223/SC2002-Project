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
    public RegistrationController(Router router, Scanner scanner, EntityStore entityStore) {
        super(router, scanner, entityStore);
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
            entityStore.append(PENDING_CR_FILE, newCR);

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

// Registration display moved to `ims.RegistrationDisplay`
