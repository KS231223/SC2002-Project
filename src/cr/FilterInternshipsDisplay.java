package cr;

import common.Controller;
import common.Display;

public class FilterInternshipsDisplay extends Display {

    public FilterInternshipsDisplay(Controller owner) {
        super(owner);
    }

    public String ask(String msg) {
        System.out.print(msg);
        return get_user_input();
    }

    @Override
    public void print_menu() {
    }
}
