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

// Withdrawal display moved to `student.WithdrawalDisplay`
