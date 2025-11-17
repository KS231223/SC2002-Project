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
        display.print_list(new ArrayList<>(pending));
        String crId = display.get_user_input().trim();
        CREntity selected = pending.stream()
            .filter(cr -> cr.get(CREntity.CRField.CRID).equals(crId))
            .findFirst()
            .orElse(null);

        if (selected == null) {
            System.out.println("Invalid ID. Returning to previous menu.");
            router.pop();
            return;
        }
        Entity userEntityToAppend = new UserEntity(
            selected.getArrayValueByIndex(0),
            selected.getArrayValueByIndex(1),
            "CR");
        display.print_entry(selected);
        String choice = display.get_user_input().trim().toUpperCase();

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

// Registration review display moved to `staff.ReviewRegistrationDisplay`