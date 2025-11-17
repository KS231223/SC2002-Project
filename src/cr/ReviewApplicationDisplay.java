package cr;

import common.Controller;
import common.Display;
import common.Entity;
import java.util.List;

public class ReviewApplicationDisplay extends Display {
    public ReviewApplicationDisplay(Controller owner) { super(owner); }

    @Override
    public void print_menu() {
        System.out.println("=== Review Internship Applications ===");
        System.out.println("Select Application ID to Approve/Reject");
    }

    public void print_entry(Entity e) {
        System.out.println("\nApplication Details: " + e);
        System.out.print("Approve (A) / Reject (R): ");
    }

    public void print_list(List<? extends Entity> list) {
        for (Entity e : list) {
            System.out.println(e.toString());
        }
        System.out.print("Enter Application ID to review: ");
    }
}
