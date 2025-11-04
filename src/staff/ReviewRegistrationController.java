package staff;

import common.*;
import java.util.*;

public class ReviewRegistrationController extends Controller {
    ReviewRegistrationDisplay display;
    private static final String PENDING_CR_FILE =
        PathResolver.resource("pending_cr.csv");
    private static final String CR_FILE =
        PathResolver.resource("cr.csv");
    private static final String USER_FILE =
        PathResolver.resource("users.csv");
    public ReviewRegistrationController(Router router, Scanner scanner, String staffID) {
        super(router, scanner);
        display = new ReviewRegistrationDisplay(this);
        router.push(this);
    }

    public void initialize() {
    List<Entity> pending = DatabaseManager.getDatabase(PENDING_CR_FILE, new ArrayList<>(), "CR");
        if (pending.isEmpty()) {
            System.out.println("No pending registrations.");
            router.pop();
            return;
        }
        display.print_list(pending);
        String CRId = display.get_user_input().trim();
    Entity crEntityToDelete = DatabaseManager.getEntryById(PENDING_CR_FILE, CRId, "CR" );

        if (crEntityToDelete == null) {
            System.out.println("Invalid ID. Returning to previous menu.");
            router.pop();
            return;
        }
        Entity userEntityToAppend = new UserEntity(crEntityToDelete.getArrayValueByIndex(0), crEntityToDelete.getArrayValueByIndex(1),"CR");
        System.out.println(userEntityToAppend);
        display.print_entry(crEntityToDelete);
        String choice = display.get_user_input().trim().toUpperCase();

        if (choice.equals("A")) {
            DatabaseManager.appendEntry(CR_FILE, crEntityToDelete);
            DatabaseManager.appendEntry(USER_FILE, userEntityToAppend);
        }
        if (choice.equals("A") || choice.equals("R")){
            DatabaseManager.deleteEntry(PENDING_CR_FILE, CRId, "CR");
        }
        System.out.println("\nReview complete.");
        router.pop();
    }
}
class ReviewRegistrationDisplay extends Display {

    public ReviewRegistrationDisplay(Controller owner) {
        super(owner);
    }

    public void print_menu() {
        System.out.println("Welcome! Choose Company Representative ID to Approve/Reject");
    }
    public void print_entry(Entity e){
        System.out.println("\nPending: " + e.toString());
        System.out.print("Approve (A) / Reject (R): ");
    }
    public void ask_for_id(){
        System.out.print("Which companyID do you wish to Approve/Reject?");

    }
    public void print_list(List<Entity> entityList){
        for( Entity e : entityList){
           System.out.println(e.toString());
        }
        System.out.println("Which entry would you like to delete?");
    }
}