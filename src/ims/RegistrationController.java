package ims;
import common.*;
import java.util.*;

public class RegistrationController extends Controller {
    Display registrationDisplay;
    private static final String PENDING_CR_FILE =
        PathResolver.resource("pending_cr.csv");

    public RegistrationController(Router router, Scanner scanner) {
        super(router, scanner);
        this.registrationDisplay = new RegistrationDisplay(this);
        router.push(this);
    }

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

    private void destroy() {
        System.out.println("\nReturning to main menu...\n");
        router.pop();
    }
}

class RegistrationDisplay extends Display {

    public RegistrationDisplay(Controller owner) {
        super(owner);
    }

    public void print_menu() {
        System.out.println("=== Company Representative Registration ===");
    }

    // Simple input collector
    public String getInput(String prompt) {
        System.out.print(prompt);
        return get_user_input();
    }
}
