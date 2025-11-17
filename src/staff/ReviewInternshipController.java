package staff;

import common.*;
import java.util.*;

/**
 * Handles staff review of pending internship submissions.
 */
public class ReviewInternshipController extends Controller {
    private final ReviewInternshipDisplay display;
    private final StaffReviewFilters filters;
    private static final String PENDING_INTERNSHIP_FILE =
        PathResolver.resource("pending_internship_opportunities.csv");
    private static final String INTERNSHIP_FILE =
        PathResolver.resource("internship_opportunities.csv");
    private static final String APPLICATION_FILE =
        PathResolver.resource("internship_applications.csv");

    /**
     * Creates a controller responsible for internship submission review.
     *
     * @param router   router used to manage controller navigation
     * @param scanner  shared input reader
     * @param staffID  identifier of the reviewing staff member
     * @param filters  shared filters to apply when listing submissions
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public ReviewInternshipController(Router router, Scanner scanner, EntityStore entityStore, String staffID, StaffReviewFilters filters) {
        super(router, scanner, entityStore);
        this.display = new ReviewInternshipDisplay(this);
        this.filters = filters;
        router.push(this);
    }

    /**
     * Lists filtered pending internships, prompts the reviewer for a decision,
     * and applies the corresponding database updates.
     */
    @Override
    public void initialize() {
        List<Entity> pendingRaw = entityStore.loadAll(PENDING_INTERNSHIP_FILE, "Internship");
        StaffReviewFilters.ApplicationStats stats = loadApplicationStats();
        List<InternshipEntity> pending = new ArrayList<>();
        for (Entity entity : pendingRaw) {
            if (entity instanceof InternshipEntity internship) {
                String id = internship.get(InternshipEntity.InternshipField.InternshipID);
                long totalApps = stats.totalFor(id);
                long acceptedApps = stats.acceptedFor(id);
                if (filters.matchesInternship(internship, totalApps, acceptedApps)) {
                    pending.add(internship);
                }
            }
        }
        if (pending.isEmpty()) {
            System.out.println("No pending internships match the current filters.");
            router.pop();
            return;
        }
        display.print_menu();
        display.print_list(new ArrayList<>(pending));
        String internshipId = display.get_user_input().trim();
        InternshipEntity internshipEntity = pending.stream()
            .filter(internship -> internship.get(InternshipEntity.InternshipField.InternshipID).equals(internshipId))
            .findFirst()
            .orElse(null);

        if (internshipEntity == null) {
            System.out.println("Invalid ID. Returning to previous menu.");
            router.pop();
            return;
        }

        display.print_entry(internshipEntity);
        String choice = display.get_user_input().trim().toUpperCase();

        if (choice.equals("A")) entityStore.append(INTERNSHIP_FILE, internshipEntity);
        if (choice.equals("A") || choice.equals("R"))
            entityStore.delete(PENDING_INTERNSHIP_FILE, internshipId, "Internship");

        System.out.println("\nReview complete.");
        router.pop();
    }

    /**
     * Builds aggregate application statistics used by the filters.
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

// Internship review display moved to `staff.ReviewInternshipDisplay`
