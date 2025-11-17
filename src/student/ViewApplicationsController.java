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

// Applications display moved to `student.ViewApplicationsDisplay`
