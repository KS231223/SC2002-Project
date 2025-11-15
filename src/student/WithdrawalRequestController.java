package student;

import common.*;
import exceptions.*;
import java.util.*;

/**
 * Enables students to submit withdrawal requests for their applications.
 */
public class WithdrawalRequestController extends StudentController {

    private final WithdrawalDisplay display;
    private static final String APPLICATION_FILE =
        PathResolver.resource("internship_applications.csv");
    private static final String WITHDRAWAL_FILE =
        PathResolver.resource("pending_withdrawal.csv");

    /**
     * Creates a controller that captures withdrawal requests.
     *
     * @param router    router managing navigation
     * @param scanner   shared console input
     * @param studentID identifier for the logged-in student
     * @throws InvalidStudentIDException when {@code studentID} cannot be resolved
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public WithdrawalRequestController(Router router, Scanner scanner, EntityStore entityStore, String studentID) throws InvalidStudentIDException {
        super(router, scanner, entityStore, studentID);
        this.display = new WithdrawalDisplay(this);
        router.push(this);
    }

    /**
     * Lists the student's applications and records a withdrawal request for the
     * chosen entry.
     */
    @Override
    public void initialize() {
        List<Entity> applications = entityStore.loadAll(APPLICATION_FILE, "Application");
        List<Entity> myApps = new ArrayList<>();

        for (Entity e : applications) {
            if (e.getArrayValueByIndex(1).equals(userID)) {
                myApps.add(e);
            }
        }

        if (myApps.isEmpty()) {
            System.out.println("No applications to withdraw from.");
            router.pop();
            return;
        }

        display.print_list(myApps);
        String appId = display.ask_app_id();
        if (appId == null) {
            System.out.println("No input captured. Returning...");
            router.pop();
            return;
        }

        String trimmedId = appId.trim();
        if ("b".equalsIgnoreCase(trimmedId)) {
            router.pop();
            return;
        }

        Entity app = entityStore.findById(APPLICATION_FILE, trimmedId, "Application");
        if (app == null || !app.getArrayValueByIndex(1).equals(userID)) {
            System.out.println("Invalid application ID.");
            router.pop();
            return;
        }

        entityStore.append(WITHDRAWAL_FILE, app);
        System.out.println("Application withdrawal submitted.");
        router.pop();
    }
}

/**
 * Display helper for the withdrawal request flow.
 */
class WithdrawalDisplay extends Display {
    /**
     * Creates a display bound to the withdrawal controller.
     *
     * @param owner controller managing this display
     */
    public WithdrawalDisplay(Controller owner) {
        super(owner);
    }

    @Override
    public void print_menu() {}

    /**
     * Prints the student's applications, allowing a withdrawal choice.
     *
     * @param apps applications available to withdraw from
     */
    public void print_list(List<Entity> apps) {
        System.out.println("=== My Applications ===");
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

    /**
     * Prompts the student for the application identifier to withdraw.
     *
     * @return user-provided application identifier
     */
    public String ask_app_id() {
        System.out.print("Enter Application ID to withdraw (or B to go back): ");
        return get_user_input();
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
}
