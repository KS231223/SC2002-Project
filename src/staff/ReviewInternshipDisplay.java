package staff;

import common.Controller;
import common.Display;
import common.Entity;
import java.util.List;

public class ReviewInternshipDisplay extends Display {
    public ReviewInternshipDisplay(Controller owner) { super(owner); }

    @Override
    public void print_menu() {
            System.out.println("Welcome! Choose Internship ID to Approve/Reject");

    }

    public void print_entry(Entity e) {
        System.out.println("\nPending internship: " + e);
        System.out.print("Approve (A) / Reject (R): ");
    }

    public void print_list(List<Entity> list) {
        list.forEach(System.out::println);
        System.out.print("Enter Internship ID to approve/reject: ");
    }
}
