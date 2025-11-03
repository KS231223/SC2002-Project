package cr;

import common.*;
import exceptions.InvalidCompanyRepIDException;
import ims.*;
import java.util.*;

public class ViewApplicationsController extends CRController {

    private static final String APPLICATION_FILE = "resources/internship_applications.csv";
    private static final String INTERNSHIP_FILE = "resources/internship_opportunities.csv";
    private ViewApplicationsDisplay display;

    public ViewApplicationsController(Router router, Scanner scanner, String crID) throws InvalidCompanyRepIDException {
        super(router, scanner, crID);
        this.display = new ViewApplicationsDisplay(this);
        router.push(this);
    }

    @Override
    public void initialize() {
        try {
            List<Entity> apps = DatabaseManager.getDatabase(APPLICATION_FILE, new ArrayList<>(), "Application");
            List<Entity> internships = DatabaseManager.getDatabase(INTERNSHIP_FILE, new ArrayList<>(), "Internship");

            Set<String> myInternshipIDs = new HashSet<>();
            for (Entity i : internships) {
                if (i.getArrayValueByIndex(9).equals(userID)) {
                    myInternshipIDs.add(i.getArrayValueByIndex(0));
                }
            }

            List<Entity> myApps = new ArrayList<>();
            for (Entity a : apps) {
                if (myInternshipIDs.contains(a.getArrayValueByIndex(2))) {
                    myApps.add(a);
                }
            }

            if (myApps.isEmpty()) {
                System.out.println("No applications for your internships.");
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

    public void print_list(List<Entity> apps) {
        System.out.println("=== Internship Applications ===");
        for (Entity e : apps) {
            System.out.println(e.toString());
        }
    }

    @Override
    public void print_menu() {}
}
