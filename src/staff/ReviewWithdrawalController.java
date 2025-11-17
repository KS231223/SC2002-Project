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
    public ReviewWithdrawalController(Router router, Scanner scanner, EntityStore entityStore, String staffID, StaffReviewFilters filters) {
        super(router, scanner, entityStore);
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
        List<Entity> pendingRaw = entityStore.loadAll(PENDING_WITHDRAWAL_FILE, "Application");
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
            ApplicationHandler handler = new ApplicationHandler(entityStore);
            handler.withdrawApplication(withdrawalId);
        }

        if (choice.equals("A") || choice.equals("R")) {
            entityStore.delete(PENDING_WITHDRAWAL_FILE, withdrawalId, "Application");
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
        List<Entity> internships = entityStore.loadAll(INTERNSHIP_FILE, "Internship");
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
        List<Entity> applications = entityStore.loadAll(APPLICATION_FILE, "Application");
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

// Review withdrawal display moved to `staff.ReviewWithdrawalDisplay`
