package student;

import common.*;
import exceptions.*;
import java.util.*;

/**
 * Lists all internship applications submitted by the current student.
 */
public class ViewApplicationsController extends StudentController {

    private final ViewApplicationsDisplay display;
    private static final String APPLICATION_FILE =
        PathResolver.resource("internship_applications.csv");

    /**
     * Creates a controller to display a student's internship applications.
     *
     * @param router    router managing navigation
     * @param scanner   shared console input
     * @param studentID identifier for the logged-in student
     * @throws InvalidStudentIDException when {@code studentID} is invalid
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public ViewApplicationsController(Router router, Scanner scanner, EntityStore entityStore, String studentID) throws InvalidStudentIDException {
        super(router, scanner, entityStore, studentID);
        this.display = new ViewApplicationsDisplay(this);
        router.push(this);
    }

    /**
     * Retrieves all applications for the current student and waits for user
     * acknowledgment before returning to the previous menu.
     */
    @Override
    public void initialize() {
        List<Entity> applications = entityStore.loadAll(APPLICATION_FILE, "Application");
        List<Entity> myApps = new ArrayList<>();

        for (Entity e : applications) {
            if (e.getArrayValueByIndex(1).equals(userID)) { // assuming ApplicationEntity order: ID, studentID, internshipID, status
                myApps.add(e);
            }
        }

        display.print_list(myApps);
        display.prompt_to_return();
        router.pop();
    }
}

/**
 * Display helper for presenting the student's applications.
 */
class ViewApplicationsDisplay extends Display {

    /**
     * Creates a display bound to the applications controller.
     *
     * @param owner controller managing this display
     */
    public ViewApplicationsDisplay(Controller owner) {
        super(owner);
    }

    @Override
    public void print_menu() {}

    /**
     * Prints the student's application list.
     *
     * @param apps applications belonging to the student
     */
    public void print_list(List<Entity> apps) {
        System.out.println("=== My Applications ===");
        if (apps.isEmpty()) {
            System.out.println("You have no applications.");
            return;
        }
        int index = 1;
        for (Entity e : apps) {
            ApplicationEntity application = (ApplicationEntity) e;
            System.out.printf("%d) Application: %s%n", index++, fallback(application.get(ApplicationEntity.ApplicationField.ApplicationID)));
            System.out.printf("   Internship ID: %s | Status: %s%n",
                fallback(application.get(ApplicationEntity.ApplicationField.InternshipID)),
                fallback(application.get(ApplicationEntity.ApplicationField.Status), "N/A"));
            System.out.printf("   Submitted: %s%n",
                fallback(application.get(ApplicationEntity.ApplicationField.SubmissionDate), "N/A"));
            System.out.println();
        }
    }

    private String fallback(String value) {
        return fallback(value, "");
    }

    private String fallback(String value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return defaultValue;
        }
        return trimmed;
    }

    public void prompt_to_return() {
        System.out.println("\nPress Enter to return or type B to go back...");
        String input = get_user_input();
        if (input != null && "b".equalsIgnoreCase(input.trim())) {
            // allowed to simply return; controller handles navigation
        }
    }
}
