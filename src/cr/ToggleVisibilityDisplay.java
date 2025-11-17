package cr;

import common.Controller;
import common.Display;
import common.Entity;
import java.util.List;

public class ToggleVisibilityDisplay extends Display {
    public ToggleVisibilityDisplay(Controller owner) {
        super(owner);
    }

    public void print_list(List<Entity> internships) {
        System.out.println("=== Your Internships ===");
        for (Entity e : internships) {
            System.out.println(e.toString());
        }
    }

    public String ask(String msg) {
        System.out.print(msg);
        return get_user_input();
    }

    @Override
    public void print_menu() {}
}
