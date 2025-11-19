package cr;

import common.*;
import exceptions.InvalidCompanyRepIDException;
import java.util.*;

/**
 * Controller that shows applications submitted to the CR's internships.
 */
public class ViewApplicationsController extends CRController {

    private static final String APPLICATION_FILE =
        PathResolver.resource("internship_applications.csv");
    private static final String INTERNSHIP_FILE =
        PathResolver.resource("internship_opportunities.csv");
    private ViewApplicationsDisplay display;

    /**
     * Builds the controller and pushes it onto the router stack.
     *
     * @param router   router orchestrating controller lifecycle
     * @param scanner  shared scanner for CLI input
     * @param entityStore persistence gateway
     * @param crID     current company representative ID
     * @throws InvalidCompanyRepIDException when the CR identifier is invalid
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public ViewApplicationsController(Router router, Scanner scanner, EntityStore entityStore, String crID) throws InvalidCompanyRepIDException {
        super(router, scanner, entityStore, crID);
        this.display = new ViewApplicationsDisplay(this);
        router.push(this);
    }

    @Override
    public void initialize() {
        try {
            List<Entity> apps = entityStore.loadAll(APPLICATION_FILE, "Application");
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

            List<ApplicationEntity> myApps = new ArrayList<>();
            for (Entity entity : apps) {
                ApplicationEntity application = (ApplicationEntity) entity;
                if (myFilteredInternships.containsKey(application.get(ApplicationEntity.ApplicationField.InternshipID))) {
                    myApps.add(application);
                }
            }

            System.out.println("Active filters: " + filters.summary());

            if (myApps.isEmpty()) {
                if (filters.hasActiveFilters()) {
                    System.out.println("No applications match the current filters.");
                } else {
                    System.out.println("No applications for your internships.");
                }
            } else {
                display.print_list(myApps);
            }
        } catch (Exception e) {
            System.err.println("Error viewing applications: " + e.getMessage());
        } finally {
            router.pop();
        }
    }
}

class ViewApplicationsDisplay extends Display {
    public ViewApplicationsDisplay(Controller owner) {
        super(owner);
    }

    /**
     * Prints the applications matching the active filters.
     *
     * @param apps application entities to render
     */
    public void print_list(List<? extends Entity> apps) {
        System.out.println("=== Internship Applications ===");
        for (Entity e : apps) {
            System.out.println(e.toString());
        }
    }

    @Override
    public void print_menu() {}
}
