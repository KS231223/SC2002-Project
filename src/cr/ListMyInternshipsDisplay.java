package cr;

import common.Controller;
import common.Display;
import common.Entity;
import java.util.List;

public class ListMyInternshipsDisplay extends Display {
    public ListMyInternshipsDisplay(Controller owner) {
        super(owner);
    }

    public void print_list(List<? extends Entity> internships) {
        System.out.println("=== Your Internships ===");
        for (Entity e : internships) {
            System.out.println(e.toString());
        }
    }

    @Override
    public void print_menu() {}
}
