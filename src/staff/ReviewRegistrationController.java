package staff;

import common.*;
import java.util.*;

/**
 * Handles review of pending company representative registrations.
 */
public class ReviewRegistrationController extends Controller {
    private final ReviewRegistrationDisplay display;
    private final StaffReviewFilters filters;
    private static final String PENDING_CR_FILE =
        PathResolver.resource("pending_cr.csv");
    private static final String CR_FILE =
        PathResolver.resource("cr.csv");
    private static final String USER_FILE =
        PathResolver.resource("users.csv");
    /**
     * Creates a controller for processing pending registrations.
     *
     * @param router  application router used for navigation
     * @param scanner shared input reader
     * @param staffID identifier of the reviewing staff member
     * @param filters shared staff filter state to apply
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public ReviewRegistrationController(Router router, Scanner scanner, EntityStore entityStore, String staffID, StaffReviewFilters filters) {
        super(router, scanner, entityStore);
        this.display = new ReviewRegistrationDisplay(this);
        this.filters = filters;
        router.push(this);
    }

    /**
     * Loads pending registrations, applies filters, and prompts the reviewer to
     * approve or reject a selected entry.
     */
    @Override
    public void initialize() {
        List<Entity> pendingRaw = entityStore.loadAll(PENDING_CR_FILE, "CR");
        List<CREntity> pending = new ArrayList<>();
        for (Entity entity : pendingRaw) {
            if (entity instanceof CREntity cr && filters.matchesRegistration(cr)) {
                pending.add(cr);
            }
        }
        if (pending.isEmpty()) {
            System.out.println("No pending registrations match the current filters.");
            router.pop();
            return;
        }
        display.print_menu();
        display.print_list(pending);
        String selection = display.get_user_input().trim();

        if (selection.equalsIgnoreCase("B")) {
            System.out.println("Returning to previous menu.");
            router.pop();
            return;
        }

        int index;
        try {
            index = Integer.parseInt(selection);
        } catch (NumberFormatException ex) {
            System.out.println("Invalid selection. Returning to previous menu.");
            router.pop();
            return;
        }

        if (index < 1 || index > pending.size()) {
            System.out.println("Invalid selection. Returning to previous menu.");
            router.pop();
            return;
        }

        CREntity selected = pending.get(index - 1);
        String crId = selected.get(CREntity.CRField.CRID);
        Entity userEntityToAppend = new UserEntity(
            selected.getArrayValueByIndex(0),
            selected.getArrayValueByIndex(1),
            "CR");
        display.print_entry(selected);
        String choice = display.get_user_input().trim().toUpperCase();

        if (choice.equals("B")) {
            System.out.println("Returning to previous menu.");
            router.pop();
            return;
        }

        if (!choice.equals("A") && !choice.equals("R")) {
            System.out.println("Invalid selection. Returning to previous menu.");
            router.pop();
            return;
        }

        if (choice.equals("A")) {
            entityStore.append(CR_FILE, selected);
            entityStore.append(USER_FILE, userEntityToAppend);
        }
        if (choice.equals("A") || choice.equals("R")){
            entityStore.delete(PENDING_CR_FILE, crId, "CR");
        }
        System.out.println("\nReview complete.");
        router.pop();
    }
}

/**
 * Display helper for the registration review flow.
 */
class ReviewRegistrationDisplay extends Display {
    private static final String LIST_ROW_FORMAT = "%-4s %-12s %-20s %-25s %-20s %-30s%n";

    /**
     * Creates a display facade for registration review.
     *
     * @param owner controller managing this display
     */
    public ReviewRegistrationDisplay(Controller owner) {
        super(owner);
    }

    /**
     * Announces the registration review prompt.
     */
    @Override
    public void print_menu() {
        System.out.println("Welcome! Select a company representative registration to review.");
    }
    /**
     * Prints the selected registration entry and prompts for an approval decision.
     *
     * @param e entity awaiting review
     */
    public void print_entry(Entity e){
        if (e == null) {
            System.out.println("\nNo entry selected.");
            return;
        }
        if (e instanceof CREntity cr) {
            System.out.println("\nPending Registration Details:");
            System.out.printf("ID: %s%n", cr.get(CREntity.CRField.CRID));
            System.out.printf("Name: %s%n", cr.get(CREntity.CRField.Name));
            System.out.printf("Company: %s%n", cr.get(CREntity.CRField.CompanyName));
            System.out.printf("Department: %s%n", cr.get(CREntity.CRField.Department));
            System.out.printf("Position: %s%n", cr.get(CREntity.CRField.Position));
            System.out.printf("Email: %s%n", cr.get(CREntity.CRField.Email));
        } else {
            System.out.println("\nPending: " + e.toString());
        }
        System.out.print("Approve (A) / Reject (R) / Back (B): ");
    }
    /**
     * Prompts the reviewer for the identifier of a registration.
     */
    public void ask_for_id(){
        System.out.print("Enter number to APPROVE/REJECT or B to go back: ");

    }
    /**
     * Shows the list of pending registrations and prompts for selection.
     *
     * @param entityList registrations available for review
     */
    public void print_list(List<CREntity> entityList){
        System.out.println("\nPending Company Representatives:");
        System.out.printf(LIST_ROW_FORMAT, "No.", "CR ID", "Name", "Company", "Position", "Email");
        for (int i = 0; i < entityList.size(); i++) {
            CREntity cr = entityList.get(i);
            System.out.printf(
                LIST_ROW_FORMAT,
                (i + 1) + ".",
                cr.get(CREntity.CRField.CRID),
                cr.get(CREntity.CRField.Name),
                cr.get(CREntity.CRField.CompanyName),
                cr.get(CREntity.CRField.Position),
                cr.get(CREntity.CRField.Email)
            );
        }
        System.out.print("\nEnter number to APPROVE/REJECT or B to go back: ");
    }
}