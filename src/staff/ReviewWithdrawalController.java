package staff;

import common.*;
import java.util.*;

public class ReviewWithdrawalController extends Controller {
    private final ReviewWithdrawalDisplay display;
    private final StaffReviewFilters filters;
    private static final String PENDING_WITHDRAWAL_FILE =
        PathResolver.resource("pending_withdrawal.csv");
    private static final String APPLICATION_FILE =
        PathResolver.resource("internship_applications.csv");
    private static final String INTERNSHIP_FILE =
        PathResolver.resource("internship_opportunities.csv");

    @SuppressWarnings("LeakingThisInConstructor")
    public ReviewWithdrawalController(Router router, Scanner scanner, String staffID, StaffReviewFilters filters) {
        super(router, scanner);
        this.display = new ReviewWithdrawalDisplay(this);
        this.filters = filters;
        router.push(this);
    }

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

        if (choice.equals("A")) ApplicationHandler.withdrawApplication(withdrawalId);
        if (choice.equals("A") || choice.equals("R"))
            DatabaseManager.deleteEntry(PENDING_WITHDRAWAL_FILE, withdrawalId, "Application");

        System.out.println("\nWithdrawal review complete.");
        router.pop();
    }

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

class ReviewWithdrawalDisplay extends Display {
    public ReviewWithdrawalDisplay(Controller owner) { super(owner); }

    @Override
    public void print_menu() {
        System.out.println("Welcome! Choose Application ID to Approve/Reject");
    }

    public void print_entry(Entity e) {
        System.out.println("\nPending withdrawal: " + e);
        System.out.print("Approve (A) / Reject (R): ");
    }

    public void print_list(List<Entity> list) {
        list.forEach(System.out::println);
        System.out.print("Enter withdrawal ID to approve/reject: ");
    }
}
