package student;

import common.*;
import ims.*;
import exceptions.*;
import java.util.*;

public class WithdrawalRequestController extends StudentController {

    private WithdrawalDisplay display;
    private static final String APPLICATION_FILE = "resources/internship_applications.csv";
    private static final String WITHDRAWAL_FILE = "resources/pending_withdrawal.csv";

    public WithdrawalRequestController(Router router, Scanner scanner, String studentID) throws InvalidStudentIDException {
        super(router, scanner, studentID);
        this.display = new WithdrawalDisplay(this);
        router.push(this);
    }

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

class WithdrawalDisplay extends Display {
    public WithdrawalDisplay(Controller owner) {
        super(owner);
    }

    @Override
    public void print_menu() {}

    public void print_list(List<Entity> apps) {
        System.out.println("=== My Applications ===");
        for (Entity e : apps) {
            System.out.println(e.toString());
        }
    }

    public String ask_app_id() {
        System.out.print("Enter Application ID to withdraw: ");
        return get_user_input();
    }
}
