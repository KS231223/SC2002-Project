package cr;

import common.*;
import exceptions.InvalidCompanyRepIDException;
import java.util.*;

public class ViewApplicationsController extends CRController {

    private static final String APPLICATION_FILE =
        PathResolver.resource("internship_applications.csv");
    private static final String INTERNSHIP_FILE =
        PathResolver.resource("internship_opportunities.csv");
    private ViewApplicationsDisplay display;

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

// View applications display moved to `cr.ViewApplicationsDisplay`
