package staff;

import common.Controller;
import common.Display;

public class StaffHomeDisplay extends Display {

    private final StaffHomePageController staffHome;

    public StaffHomeDisplay(Controller owner) {
        super(owner);
        this.staffHome = (StaffHomePageController) owner;
    }

    @Override
    public void print_menu() {
        System.out.println("Welcome! Displaying staff dashboard...");
        System.out.println("=== Staff Home Page ===");
        System.out.println("=== Career Center Staff Menu ===");
        staffHome.printFilterSummary();
        System.out.println("1. Review company representative registrations");
        System.out.println("2. Review internship submissions");
        System.out.println("3. Handle withdrawal requests");
        System.out.println("4. Update review filters");
        System.out.println("5. Clear review filters");
        System.out.println("6. Generate internship report");
        System.out.println("7. Change password");
        System.out.println("8. Logout");
        System.out.print("Select an option: ");
    }
}
