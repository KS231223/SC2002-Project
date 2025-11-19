package cr;

import common.*;
import exceptions.InvalidCompanyRepIDException;
import java.util.*;

/**
 * Controller that lists internships owned by the authenticated company representative.
 */
public class ListMyInternshipsController extends CRController {

    private static final String INTERNSHIP_FILE =
        PathResolver.resource("internship_opportunities.csv");
    private ListMyInternshipsDisplay display;

    /**
     * Creates the controller and registers it with the router stack.
     *
     * @param router   navigation router for view transitions
     * @param scanner  shared scanner for console input
     * @param entityStore backing store for internship data
     * @param crID     company representative identifier
     * @throws InvalidCompanyRepIDException if the CR identifier is invalid
     */
    public ListMyInternshipsController(Router router, Scanner scanner, EntityStore entityStore, String crID) throws InvalidCompanyRepIDException {
        super(router, scanner, entityStore, crID);
        this.display = new ListMyInternshipsDisplay(this);
        router.push(this);
    }

    @Override
    public void initialize() {
        try {
            List<Entity> allInternships = entityStore.loadAll(INTERNSHIP_FILE, "Internship");
            CRFilterService.CRFilters filters = CRFilterService.getFilters(userID);
            List<InternshipEntity> filteredInternships = new ArrayList<>();

            for (Entity entity : allInternships) {
                InternshipEntity internship = (InternshipEntity) entity;
                if (!CRFilterService.belongsToCompany(internship, companyName)) {
                    continue;
                }
                if (CRFilterService.matchesInternship(internship, filters)) {
                    filteredInternships.add(internship);
                }
            }

            System.out.println("Active filters: " + filters.summary());

            if (filteredInternships.isEmpty()) {
                if (filters.hasActiveFilters()) {
                    System.out.println("No internships match the current filters.");
                } else {
                    System.out.println("You currently have no internships listed.");
                }
            } else {
                display.print_list(filteredInternships);
            }
        } catch (Exception e) {
            System.err.println("Error displaying internships: " + e.getMessage());
        } finally {
            router.pop();
        }
    }
}

class ListMyInternshipsDisplay extends Display {
    public ListMyInternshipsDisplay(Controller owner) {
        super(owner);
    }

    /**
     * Prints the filtered internship list.
     *
     * @param internships internships belonging to the active company
     */
    public void print_list(List<? extends Entity> internships) {
        System.out.println("=== Your Internships ===");
        for (Entity e : internships) {
            System.out.println(e.toString());
        }
    }

    @Override
    public void print_menu() {}
}
