package staff;

import common.Controller;
import common.Display;
import common.Entity;
import java.util.List;

public class ReviewRegistrationDisplay extends Display {
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
