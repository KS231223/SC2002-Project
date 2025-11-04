package staff;

import common.*;
import java.util.*;

public class ReviewWithdrawalController extends Controller {
    ReviewWithdrawalDisplay display;
    private static final String PENDING_WITHDRAWAL_FILE =
        PathResolver.resource("pending_withdrawal.csv");
    private static final String APPLICATION_FILE =
        PathResolver.resource("internship_applications.csv");

    public ReviewWithdrawalController(Router router, Scanner scanner, String staffID) {
        super(router, scanner);
        display = new ReviewWithdrawalDisplay(this);
        router.push(this);
    }

    public void initialize() {
    List<Entity> pending = DatabaseManager.getDatabase(PENDING_WITHDRAWAL_FILE, new ArrayList<>(), "Application");
        if (pending.isEmpty()) {
            System.out.println("No pending withdrawals.");
            router.pop();
            return;
        }
        display.print_menu();
        display.print_list(pending);
        String withdrawalId = display.get_user_input().trim();
    Entity withdrawalEntity = DatabaseManager.getEntryById(PENDING_WITHDRAWAL_FILE, withdrawalId, "Application");

        if (withdrawalEntity == null) {
            System.out.println("Invalid ID. Returning to previous menu.");
            router.pop();
            return;
        }

        display.print_entry(withdrawalEntity);
        String choice = display.get_user_input().trim().toUpperCase();

        if (choice.equals("A")) ApplicationHandler.withdrawApplication(withdrawalId);
        if (choice.equals("A") || choice.equals("R"))
            DatabaseManager.deleteEntry(PENDING_WITHDRAWAL_FILE, withdrawalId, "Application");

        System.out.println("\nWithdrawal review complete.");
        router.pop();
    }

    private void handleApprovedWithdrawal(String appId) {
    Entity application = DatabaseManager.getEntryById(APPLICATION_FILE, appId, "Application");

        if (application == null) {
            System.out.println("No such application found (unexpected).");
            return;
        }
        int statusKey = ApplicationEntity.ApplicationField.Status.ordinal();
        String currentStatus = application.getArrayValueByIndex(statusKey);
        if (currentStatus.equalsIgnoreCase("ACCEPTED")) {
            System.out.println("Application was accepted; handle post-withdrawal consequences later.");
        }

        application.setArrayValueByIndex(statusKey,"WITHDRAWN");
    DatabaseManager.updateEntry(APPLICATION_FILE, appId, application, "Application");
        System.out.println("Application marked as WITHDRAWN.");
    }
}

class ReviewWithdrawalDisplay extends Display {
    public ReviewWithdrawalDisplay(Controller owner) { super(owner); }

    @Override
    public void print_menu() {
        System.out.println("Welcome! Choose Application ID to Approve/Reject");
    }

    public void print_entry(Entity e) {
        System.out.println("\nPending withdrawal: " + e);
        System.out.print("Approve (A) / Reject (R): ");
    }

    public void print_list(List<Entity> list) {
        list.forEach(System.out::println);
        System.out.print("Enter withdrawal ID to approve/reject: ");
    }
}
