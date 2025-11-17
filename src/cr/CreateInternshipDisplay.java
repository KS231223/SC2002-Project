package cr;

import common.Controller;
import common.Display;

public class CreateInternshipDisplay extends Display {
    public CreateInternshipDisplay(Controller owner) {
        super(owner);
    }

    public String ask(String msg) {
        System.out.print(msg);
        return get_user_input().trim();
    }

    @Override
    public void print_menu() {}
}
