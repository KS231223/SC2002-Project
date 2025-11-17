package cr;

import common.Controller;
import common.Display;
import common.Entity;
import java.util.List;

public class ViewApplicationsDisplay extends Display {
    public ViewApplicationsDisplay(Controller owner) {
        super(owner);
    }

    public void print_list(List<? extends Entity> apps) {
        System.out.println("=== Internship Applications ===");
        for (Entity e : apps) {
            System.out.println(e.toString());
        }
    }

    @Override
    public void print_menu() {}
}
