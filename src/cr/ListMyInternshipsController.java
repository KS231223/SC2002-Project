package cr;

import common.*;
import exceptions.InvalidCompanyRepIDException;
import java.util.*;

public class ListMyInternshipsController extends CRController {

    private static final String INTERNSHIP_FILE =
        PathResolver.resource("internship_opportunities.csv");
    private ListMyInternshipsDisplay display;

    public ListMyInternshipsController(Router router, Scanner scanner, String crID) throws InvalidCompanyRepIDException {
        super(router, scanner, crID);
        this.display = new ListMyInternshipsDisplay(this);
        router.push(this);
    }

    @Override
    public void initialize() {
        try {
            List<Entity> allInternships = DatabaseManager.getDatabase(INTERNSHIP_FILE, new ArrayList<>(), "Internship");
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

    public void print_list(List<? extends Entity> internships) {
        System.out.println("=== Your Internships ===");
        for (Entity e : internships) {
            System.out.println(e.toString());
        }
    }

    @Override
    public void print_menu() {}
}
