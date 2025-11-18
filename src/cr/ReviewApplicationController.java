package cr;

import common.*;
import exceptions.InvalidCompanyRepIDException;
import java.util.*;

public class ReviewApplicationController extends CRController {
    private final ReviewApplicationDisplay display;
    private static final String APPLICATION_FILE =
        PathResolver.resource("internship_applications.csv");
    private static final String INTERNSHIP_FILE =
        PathResolver.resource("internship_opportunities.csv");

    @SuppressWarnings("LeakingThisInConstructor")
    public ReviewApplicationController(Router router, Scanner scanner, EntityStore entityStore, String crID) throws InvalidCompanyRepIDException {
        super(router, scanner, entityStore, crID);
        this.display = new ReviewApplicationDisplay(this);
        router.push(this);
    }

    @Override
    public void initialize() {
        List<Entity> applications = entityStore.loadAll(APPLICATION_FILE, "Application");
        List<Entity> internships = entityStore.loadAll(INTERNSHIP_FILE, "Internship");
        CRFilterService.CRFilters filters = CRFilterService.getFilters(userID);

        Map<String, InternshipEntity> myFilteredInternships = new HashMap<>();
        for (Entity entity : internships) {
            InternshipEntity internship = (InternshipEntity) entity;
            if (!CRFilterService.belongsToCompany(internship, companyName)) {
                continue;
            }
            if (CRFilterService.matchesInternship(internship, filters)) {
                myFilteredInternships.put(internship.get(InternshipEntity.InternshipField.InternshipID), internship);
            }
        }

        Map<String, ApplicationEntity> reviewableApplications = new LinkedHashMap<>();
        for (Entity entity : applications) {
            ApplicationEntity application = (ApplicationEntity) entity;
            if (myFilteredInternships.containsKey(application.get(ApplicationEntity.ApplicationField.InternshipID))) {
                reviewableApplications.put(application.get(ApplicationEntity.ApplicationField.ApplicationID), application);
            }
        }

        System.out.println("Active filters: " + filters.summary());

        if (reviewableApplications.isEmpty()) {
            if (filters.hasActiveFilters()) {
                System.out.println("No applications match the current filters.");
            } else {
                System.out.println("No applications to review.");
            }
            router.pop();
            return;
        }

        display.print_menu();
        display.print_list(new ArrayList<>(reviewableApplications.values()));

        String applicationId = display.get_user_input().trim();
        ApplicationEntity application = reviewableApplications.get(applicationId);
        if (application == null) {
            System.out.println("Invalid Application ID. Returning to previous menu.");
            router.pop();
            return;
        }

        display.print_entry(application);
        String choice = display.get_user_input().trim().toUpperCase(Locale.ENGLISH);

        ApplicationHandler handler = new ApplicationHandler(entityStore);
        try {
            switch (choice) {
                case "A" -> handler.approveApplication(applicationId);
                case "R" -> {
                    application.set(ApplicationEntity.ApplicationField.Status, "Rejected");
                    entityStore.update(APPLICATION_FILE, applicationId, application, "Application");
                    System.out.println("Application rejected successfully.");
                }
                default -> System.out.println("Invalid choice. Returning...");
            }
        } catch (Exception e) {
            System.err.println("Error while reviewing application: " + e.getMessage());
        }

        System.out.println("\nReview complete.");
        router.pop();
    }
}

class ReviewApplicationDisplay extends Display {
    public ReviewApplicationDisplay(Controller owner) { super(owner); }

    @Override
    public void print_menu() {
        System.out.println("=== Review Internship Applications ===");
        System.out.println("Select Application ID to Approve/Reject");
    }

    public void print_entry(Entity e) {
        System.out.println("\nApplication Details: " + e);
        System.out.print("Approve (A) / Reject (R): ");
    }

    public void print_list(List<? extends Entity> list) {
        for (Entity e : list) {
            System.out.println(e.toString());
        }
        System.out.print("Enter Application ID to review: ");
    }
}
