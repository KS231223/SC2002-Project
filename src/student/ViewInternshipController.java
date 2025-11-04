package student;

import common.*;
import exceptions.*;
import java.util.*;

public class ViewInternshipController extends StudentController {

    private ViewInternshipDisplay display;
    private static final String INTERNSHIP_FILE =
        PathResolver.resource("internship_opportunities.csv");

    public ViewInternshipController(Router router, Scanner scanner, String studentID) throws InvalidStudentIDException {
        super(router, scanner, studentID);
        this.display = new ViewInternshipDisplay(this);
        router.push(this);
    }

    @Override
    public void initialize() {
        List<Entity> internships = DatabaseManager.getDatabase(INTERNSHIP_FILE, new ArrayList<>(), "Internship");

        if (internships.isEmpty()) {
            System.out.println("No internships available at the moment.");
            router.pop();
            return;
        }
        display.print_menu();
        display.print_list(internships);
        System.out.println("\nPress Enter to return...");
        display.get_user_input();
        router.pop();
    }
}

class ViewInternshipDisplay extends Display {

    public ViewInternshipDisplay(Controller owner) {
        super(owner);
    }

    @Override
    public void print_menu() {
        System.out.println("Printing out Internships based on filters!");
    }

    public void print_list(List<Entity> internships) {
        System.out.println("=== Available Internships ===");
        for (Entity e : internships) {
            System.out.println(e.toString());
        }
    }
}
