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
    public WithdrawalRequestController(Router router, Scanner scanner, String studentID) throws InvalidStudentIDException {
        super(router, scanner, studentID);
        this.display = new WithdrawalDisplay(this);
        router.push(this);
    }

    /**
     * Lists the student's applications and records a withdrawal request for the
     * chosen entry.
     */
    @Override
    public void initialize() {
        List<Entity> applications = DatabaseManager.getDatabase(APPLICATION_FILE, new ArrayList<>(), "Application");
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

        Entity app = DatabaseManager.getEntryById(APPLICATION_FILE, appId, "Application");
        if (app == null || !app.getArrayValueByIndex(1).equals(userID)) {
            System.out.println("Invalid application ID.");
            router.pop();
            return;
        }

        DatabaseManager.appendEntry(WITHDRAWAL_FILE, app);
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
        for (Entity e : apps) {
            System.out.println(e.toString());
        }
    }

    /**
     * Prompts the student for the application identifier to withdraw.
     *
     * @return user-provided application identifier
     */
    public String ask_app_id() {
        System.out.print("Enter Application ID to withdraw: ");
        return get_user_input();
    }
}
