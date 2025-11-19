package student;

import common.*;
import exceptions.*;
import java.util.*;

/**
 * Controller that lets a student review the complete history of applications submitted.
 */
public class ViewApplicationHistoryController extends StudentController {

    private ViewApplicationHistoryDisplay display;
    private static final String APPLICATION_FILE =
        PathResolver.resource("internship_applications.csv");
    private static final String INTERNSHIP_FILE =
        PathResolver.resource("internship_opportunities.csv");

    /**
     * Creates the controller and registers it with the router.
     *
     * @param router    navigation router controlling screen stack
     * @param scanner   shared scanner for CLI prompts
     * @param entityStore persistence boundary for student data needs
     * @param studentID current authenticated student identifier
     * @throws InvalidStudentIDException when the ID fails validation
     */
    public ViewApplicationHistoryController(Router router, Scanner scanner, EntityStore entityStore, String studentID) throws InvalidStudentIDException {
        super(router, scanner, entityStore, studentID);
        this.display = new ViewApplicationHistoryDisplay(this);
        router.push(this);
    }

    @Override
    public void initialize() {
        List<Entity> applications = entityStore.loadAll(APPLICATION_FILE, "Application");
        List<Entity> internships = entityStore.loadAll(INTERNSHIP_FILE, "Internship");
        List<ApplicationWithDetails> applicationHistory = new ArrayList<>();

        for (Entity app : applications) {
            if (app.getArrayValueByIndex(1).equals(studentID)) { // Check if this application belongs to the student
                String internshipID = app.getArrayValueByIndex(2);
                String internshipTitle = "Unknown";
                for (Entity internship : internships) {
                    if (internship.getArrayValueByIndex(0).equals(internshipID)) {
                        internshipTitle = internship.getArrayValueByIndex(1);
                        break;
                    }
                }
                applicationHistory.add(new ApplicationWithDetails(
                    app.getArrayValueByIndex(0), // Application ID
                    internshipID,
                    internshipTitle,
                    app.getArrayValueByIndex(3), // Status
                    app.getArrayValueByIndex(4)  // Submission Date
                ));
            }
        }

        display.print_list(applicationHistory);
        System.out.println("\nPress Enter to return...");
        display.get_user_input();
        router.pop();
    }

    /**
     * Helper class bundling application and internship metadata for display.
     */
    public static class ApplicationWithDetails {
        public String applicationID;
        public String internshipID;
        public String internshipTitle;
        public String status;
        public String submissionDate;

        public ApplicationWithDetails(String applicationID, String internshipID, String internshipTitle, String status, String submissionDate) {
            this.applicationID = applicationID;
            this.internshipID = internshipID;
            this.internshipTitle = internshipTitle;
            this.status = status;
            this.submissionDate = submissionDate;
        }

        @Override
        public String toString() {
            return "Application ID: " + applicationID +
                    " | Internship: " + internshipTitle +
                    " | Status: " + status +
                    " | Submission Date: " + submissionDate;
        }
    }
}

class ViewApplicationHistoryDisplay extends Display {

    public ViewApplicationHistoryDisplay(Controller owner) {
        super(owner);
    }

    @Override
    public void print_menu() {}

    /**
     * Renders the full application history for the current student.
     *
     * @param applications resolved application list enriched with internship data
     */
    public void print_list(List<ViewApplicationHistoryController.ApplicationWithDetails> applications) {
        System.out.println("=== Application History ===");
        if (applications.isEmpty()) {
            System.out.println("You have no application history.");
            return;
        }
        for (int i = 0; i < applications.size(); i++) {
            System.out.println((i + 1) + ". " + applications.get(i).toString());
        }
    }
}
