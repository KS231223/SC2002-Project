package staff;

import common.*;
import java.util.*;

public class ReviewRegistrationController extends Controller {
    private final ReviewRegistrationDisplay display;
    private final StaffReviewFilters filters;
    private static final String PENDING_CR_FILE =
        PathResolver.resource("pending_cr.csv");
    private static final String CR_FILE =
        PathResolver.resource("cr.csv");
    private static final String USER_FILE =
        PathResolver.resource("users.csv");
    @SuppressWarnings("LeakingThisInConstructor")
    public ReviewRegistrationController(Router router, Scanner scanner, String staffID, StaffReviewFilters filters) {
        super(router, scanner);
        this.display = new ReviewRegistrationDisplay(this);
        this.filters = filters;
        router.push(this);
    }

    @Override
    public void initialize() {
        List<Entity> pendingRaw = DatabaseManager.getDatabase(PENDING_CR_FILE, new ArrayList<>(), "CR");
        List<CREntity> pending = new ArrayList<>();
        for (Entity entity : pendingRaw) {
            if (entity instanceof CREntity cr && filters.matchesRegistration(cr)) {
                pending.add(cr);
            }
        }
        if (pending.isEmpty()) {
            System.out.println("No pending registrations match the current filters.");
            router.pop();
            return;
        }
        display.print_menu();
        display.print_list(new ArrayList<>(pending));
        String crId = display.get_user_input().trim();
        CREntity selected = pending.stream()
            .filter(cr -> cr.get(CREntity.CRField.CRID).equals(crId))
            .findFirst()
            .orElse(null);

        if (selected == null) {
            System.out.println("Invalid ID. Returning to previous menu.");
            router.pop();
            return;
        }
        Entity userEntityToAppend = new UserEntity(
            selected.getArrayValueByIndex(0),
            selected.getArrayValueByIndex(1),
            "CR");
        display.print_entry(selected);
        String choice = display.get_user_input().trim().toUpperCase();

        if (choice.equals("A")) {
            DatabaseManager.appendEntry(CR_FILE, selected);
            DatabaseManager.appendEntry(USER_FILE, userEntityToAppend);
        }
        if (choice.equals("A") || choice.equals("R")){
            DatabaseManager.deleteEntry(PENDING_CR_FILE, crId, "CR");
        }
        System.out.println("\nReview complete.");
        router.pop();
    }
}
class ReviewRegistrationDisplay extends Display {

    public ReviewRegistrationDisplay(Controller owner) {
        super(owner);
    }

    @Override
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
    public void print_list(List<? extends Entity> entityList){
        for (Entity e : entityList){
           System.out.println(e.toString());
        }
        System.out.println("Which entry would you like to delete?");
    }
}