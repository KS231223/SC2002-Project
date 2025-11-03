package cr;

import common.*;
import exceptions.InvalidCompanyRepIDException;
import ims.*;
import java.util.*;

public class ListMyInternshipsController extends CRController {

    private static final String INTERNSHIP_FILE = "resources/internship_opportunities.csv";
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
            List<Entity> myInternships = new ArrayList<>();

            for (Entity e : allInternships) {
                if (e.getArrayValueByIndex(9).equals(userID)) { // CRInCharge column
                    myInternships.add(e);
                }
            }

            if (myInternships.isEmpty()) {
                System.out.println("You currently have no internships listed.");
            } else {
                display.print_list(myInternships);
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

    public void print_list(List<Entity> internships) {
        System.out.println("=== Your Internships ===");
        for (Entity e : internships) {
            System.out.println(e.toString());
        }
    }

    @Override
    public void print_menu() {}
}
