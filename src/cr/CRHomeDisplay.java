package cr;

import common.Controller;
import common.Display;

public class CRHomeDisplay extends Display {

    public CRHomeDisplay(Controller owner) {
        super(owner);
    }

    @Override
    public void print_menu() {
        System.out.println("=== Company Representative Menu ===");
        System.out.println("1. List my internships");
        System.out.println("2. Create internship");
        System.out.println("3. Toggle internship visibility");
        System.out.println("4. View applications for my internships");
        System.out.println("5. Review internship application");
        System.out.println("6. Update internship filters");
        System.out.println("7. Clear internship filters");
        System.out.println("8. Change password");
        System.out.println("9. Logout");
        System.out.print("Select an option: ");
    }
}
