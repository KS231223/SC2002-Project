package staff;

import common.*;
import java.util.*;

public class ReviewInternshipController extends Controller {
    private final ReviewInternshipDisplay display;
    private final StaffReviewFilters filters;
    private static final String PENDING_INTERNSHIP_FILE =
        PathResolver.resource("pending_internship_opportunities.csv");
    private static final String INTERNSHIP_FILE =
        PathResolver.resource("internship_opportunities.csv");
    private static final String APPLICATION_FILE =
        PathResolver.resource("internship_applications.csv");

    @SuppressWarnings("LeakingThisInConstructor")
    public ReviewInternshipController(Router router, Scanner scanner, String staffID, StaffReviewFilters filters) {
        super(router, scanner);
        this.display = new ReviewInternshipDisplay(this);
        this.filters = filters;
        router.push(this);
    }

    @Override
    public void initialize() {
        List<Entity> pendingRaw = DatabaseManager.getDatabase(PENDING_INTERNSHIP_FILE, new ArrayList<>(), "Internship");
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

        if (choice.equals("A")) DatabaseManager.appendEntry(INTERNSHIP_FILE, internshipEntity);
        if (choice.equals("A") || choice.equals("R"))
            DatabaseManager.deleteEntry(PENDING_INTERNSHIP_FILE, internshipId, "Internship");

        System.out.println("\nReview complete.");
        router.pop();
    }

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

class ReviewInternshipDisplay extends Display {
    public ReviewInternshipDisplay(Controller owner) { super(owner); }

    @Override
    public void print_menu() {
            System.out.println("Welcome! Choose Internship ID to Approve/Reject");

    }

    public void print_entry(Entity e) {
        System.out.println("\nPending internship: " + e);
        System.out.print("Approve (A) / Reject (R): ");
    }

    public void print_list(List<Entity> list) {
        list.forEach(System.out::println);
        System.out.print("Enter Internship ID to approve/reject: ");
    }
}
