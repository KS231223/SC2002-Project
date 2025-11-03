package staff;

import common.*;
import java.util.*;

public class ReviewWithdrawalController extends Controller {
    ReviewWithdrawalDisplay display;
    String filePath = "resources/pending_withdrawal.csv";

    public ReviewWithdrawalController(Router router, Scanner scanner, String staffID) {
        super(router, scanner);
        display = new ReviewWithdrawalDisplay(this);
        router.push(this);
    }

    public void initialize() {
        List<Entity> pending = DatabaseManager.getDatabase(filePath, new ArrayList<>(), "Application");
        if (pending.isEmpty()) {
            System.out.println("No pending withdrawals.");
            router.pop();
            return;
        }
        display.print_menu();
        display.print_list(pending);
        String withdrawalId = display.get_user_input().trim();
        Entity withdrawalEntity = DatabaseManager.getEntryById(filePath, withdrawalId, "Application");

        if (withdrawalEntity == null) {
            System.out.println("Invalid ID. Returning to previous menu.");
            router.pop();
            return;
        }

        display.print_entry(withdrawalEntity);
        String choice = display.get_user_input().trim().toUpperCase();

        if (choice.equals("A")) handleApprovedWithdrawal(withdrawalId);
        if (choice.equals("A") || choice.equals("R"))
            DatabaseManager.deleteEntry(filePath, withdrawalId, "Application");

        System.out.println("\nWithdrawal review complete.");
        router.pop();
    }

    private void handleApprovedWithdrawal(String appId) {
        Entity application = DatabaseManager.getEntryById("resources/internship_applications.csv", appId, "Application");

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
        DatabaseManager.updateEntry("resources/internship_applications.csv", appId, application, "Application");
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
