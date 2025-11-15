package staff;

import common.*;
import java.util.*;

/**
 * Coordinates staff review of pending internship withdrawal requests.
 */
public class ReviewWithdrawalController extends Controller {
    private final ReviewWithdrawalDisplay display;
    private final StaffReviewFilters filters;
    private static final String PENDING_WITHDRAWAL_FILE =
            PathResolver.resource("pending_withdrawal.csv");
    private static final String APPLICATION_FILE =
            PathResolver.resource("internship_applications.csv");
    private static final String INTERNSHIP_FILE =
            PathResolver.resource("internship_opportunities.csv");

    /**
     * Creates a withdrawal review controller.
     *
     * @param router  router managing navigation
     * @param scanner shared input reader
     * @param staffID identifier of the reviewing staff member
     * @param filters shared filters to apply across review flows
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public ReviewWithdrawalController(Router router, Scanner scanner, String staffID, StaffReviewFilters filters) {
        super(router, scanner);
        this.display = new ReviewWithdrawalDisplay(this);
        this.filters = filters;
        router.push(this);
    }

    /**
     * Lists filtered withdrawal requests, prompts for decisions, and applies
     * the resulting updates to the database.
     */
    @Override
    public void initialize() {
        List<Entity> pendingRaw = DatabaseManager.getDatabase(PENDING_WITHDRAWAL_FILE, new ArrayList<>(), "Application");
        if (pendingRaw.isEmpty()) {
            System.out.println("No pending withdrawals.");
            router.pop();
            return;
        }

        Map<String, InternshipEntity> internships = loadInternships();
        StaffReviewFilters.ApplicationStats stats = loadApplicationStats();
        List<ApplicationEntity> pending = new ArrayList<>();

        for (Entity entity : pendingRaw) {
            if (entity instanceof ApplicationEntity application) {
                String internshipId = application.get(ApplicationEntity.ApplicationField.InternshipID);
                InternshipEntity internship = internships.get(internshipId);
                long totalApps = stats.totalFor(internshipId);
                long acceptedApps = stats.acceptedFor(internshipId);

                if (filters.matchesWithdrawal(application, internship, totalApps, acceptedApps)) {
                    pending.add(application);
                }
            }
        }

        if (pending.isEmpty()) {
            System.out.println("No pending withdrawals match the current filters.");
            router.pop();
            return;
        }

        display.print_menu();
        display.print_list(new ArrayList<>(pending));

        String withdrawalId = display.get_user_input().trim();
        ApplicationEntity withdrawalEntity = pending.stream()
                .filter(app -> app.get(ApplicationEntity.ApplicationField.ApplicationID).equals(withdrawalId))
                .findFirst()
                .orElse(null);

        if (withdrawalEntity == null) {
            System.out.println("Invalid ID. Returning to previous menu.");
            router.pop();
            return;
        }

        display.print_entry(withdrawalEntity);
        String choice = display.get_user_input().trim().toUpperCase();

        // Delegate to ApplicationHandler
        if (choice.equals("A")) {
            ApplicationHandler.withdrawApplication(withdrawalId);
        }

        if (choice.equals("A") || choice.equals("R")) {
            DatabaseManager.deleteEntry(PENDING_WITHDRAWAL_FILE, withdrawalId, "Application");
        }

        System.out.println("\nWithdrawal review complete.");
        router.pop();
    }

    /**
     * Loads internship records to pair with withdrawals.
     *
     * @return map keyed by internship identifier
     */
    private Map<String, InternshipEntity> loadInternships() {
        List<Entity> internships = DatabaseManager.getDatabase(INTERNSHIP_FILE, new ArrayList<>(), "Internship");
        Map<String, InternshipEntity> result = new HashMap<>();
        for (Entity entity : internships) {
            if (entity instanceof InternshipEntity internship) {
                result.put(internship.get(InternshipEntity.InternshipField.InternshipID), internship);
            }
        }
        return result;
    }

    /**
     * Builds application statistics required by the filters.
     *
     * @return statistics bundle keyed by internship identifier
     */
    private StaffReviewFilters.ApplicationStats loadApplicationStats() {
        List<Entity> applications = DatabaseManager.getDatabase(APPLICATION_FILE, new ArrayList<>(), "Application");
        Map<String, Long> total = new HashMap<>();
        Map<String, Long> accepted = new HashMap<>();

        for (Entity entity : applications) {
            if (entity instanceof ApplicationEntity application) {
                String internshipId = application.get(ApplicationEntity.ApplicationField.InternshipID);
                total.merge(internshipId, 1L, Long::sum);
                if ("ACCEPTED".equalsIgnoreCase(application.get(ApplicationEntity.ApplicationField.Status))) {
                    accepted.merge(internshipId, 1L, Long::sum);
                }
            }
        }

        return new StaffReviewFilters.ApplicationStats(total, accepted);
    }
}

/**
 * Display helper for the withdrawal review workflow.
 */
class ReviewWithdrawalDisplay extends Display {
    /**
     * Creates a display helper for withdrawal review interactions.
     *
     * @param owner controller managing this display
     */
    public ReviewWithdrawalDisplay(Controller owner) { super(owner); }

    /**
     * Announces the withdrawal review prompt to the reviewer.
     */
    @Override
    public void print_menu() {
        System.out.println("Welcome! Choose Application ID to Approve/Reject");
    }

    /**
     * Shows the selected withdrawal details and prompts for approval.
     *
     * @param e withdrawal request pending review
     */
    public void print_entry(Entity e) {
        System.out.println("\nPending withdrawal: " + e);
        System.out.print("Approve (A) / Reject (R): ");
    }

    /**
     * Prints the withdrawals available for review and prompts for a selection.
     *
     * @param list collection of withdrawals available for selection
     */
    public void print_list(List<Entity> list) {
        list.forEach(System.out::println);
        System.out.print("Enter withdrawal ID to approve/reject: ");
    }
}
