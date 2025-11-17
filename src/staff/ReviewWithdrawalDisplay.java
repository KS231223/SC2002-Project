package staff;

import common.Controller;
import common.Display;
import common.Entity;
import java.util.List;

public class ReviewWithdrawalDisplay extends Display {
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
