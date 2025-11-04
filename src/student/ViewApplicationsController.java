package student;

import common.*;
import exceptions.*;
import java.util.*;

public class ViewApplicationsController extends StudentController {

    private ViewApplicationsDisplay display;
    private static final String APPLICATION_FILE =
        PathResolver.resource("internship_applications.csv");

    public ViewApplicationsController(Router router, Scanner scanner, String studentID) throws InvalidStudentIDException {
        super(router, scanner, studentID);
        this.display = new ViewApplicationsDisplay(this);
        router.push(this);
    }

    @Override
    public void initialize() {
        List<Entity> applications = DatabaseManager.getDatabase(APPLICATION_FILE, new ArrayList<>(), "Application");
        List<Entity> myApps = new ArrayList<>();

        for (Entity e : applications) {
            if (e.getArrayValueByIndex(1).equals(userID)) { // assuming ApplicationEntity order: ID, studentID, internshipID, status
                myApps.add(e);
            }
        }

        display.print_list(myApps);
        System.out.println("\nPress Enter to return...");
        display.get_user_input();
        router.pop();
    }
}

class ViewApplicationsDisplay extends Display {

    public ViewApplicationsDisplay(Controller owner) {
        super(owner);
    }

    @Override
    public void print_menu() {}

    public void print_list(List<Entity> apps) {
        System.out.println("=== My Applications ===");
        if (apps.isEmpty()) {
            System.out.println("You have no applications.");
            return;
        }
        for (Entity e : apps) {
            System.out.println(e.toString());
        }
    }
}
