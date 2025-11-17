package student;

import common.Controller;
import common.Display;
import common.ApplicationEntity;
import common.Entity;
import java.util.List;

public class AcceptOfferDisplay extends Display {

    public AcceptOfferDisplay(Controller owner) {
        super(owner);
    }

    @Override
    public void print_menu() {}

    public void print_list(List<Entity> offers) {
        System.out.println("=== Internship Offers ===");
        int index = 1;
        for (Entity e : offers) {
            ApplicationEntity offer = (ApplicationEntity) e;
            System.out.printf("%d) Application: %s%n", index++, fallback(offer.get(ApplicationEntity.ApplicationField.ApplicationID)));
            System.out.printf("   Internship ID: %s | Status: %s%n",
                fallback(offer.get(ApplicationEntity.ApplicationField.InternshipID)),
                fallback(offer.get(ApplicationEntity.ApplicationField.Status), "N/A"));
            System.out.printf("   Submitted: %s%n",
                fallback(offer.get(ApplicationEntity.ApplicationField.SubmissionDate), "N/A"));
            System.out.println();
        }
    }

    public String ask_app_id() {
        System.out.print("Enter Application ID to accept (or B to go back): ");
        return get_user_input();
    }

    private String fallback(String value) {
        return fallback(value, "");
    }

    private String fallback(String value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return defaultValue;
        }
        return trimmed;
    }
}
