package student;

import common.Controller;
import common.Display;
import common.ApplicationEntity;
import common.Entity;
import java.util.List;

public class ViewApplicationsDisplay extends Display {

    public ViewApplicationsDisplay(Controller owner) {
        super(owner);
    }

    @Override
    public void print_menu() {}

    public void print_list(List<Entity> apps) {
        System.out.println("=== My Applications ===");
        if (apps.isEmpty()) {
            System.out.println("You have no applications.");
            return;
        }
        int index = 1;
        for (Entity e : apps) {
            ApplicationEntity application = (ApplicationEntity) e;
            System.out.printf("%d) Application: %s%n", index++, fallback(application.get(ApplicationEntity.ApplicationField.ApplicationID)));
            System.out.printf("   Internship ID: %s | Status: %s%n",
                fallback(application.get(ApplicationEntity.ApplicationField.InternshipID)),
                fallback(application.get(ApplicationEntity.ApplicationField.Status), "N/A"));
            System.out.printf("   Submitted: %s%n",
                fallback(application.get(ApplicationEntity.ApplicationField.SubmissionDate), "N/A"));
            System.out.println();
        }
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

    public void prompt_to_return() {
        System.out.println("\nPress Enter to return or type B to go back...");
        String input = get_user_input();
        if (input != null && "b".equalsIgnoreCase(input.trim())) {
            // controller handles navigation
        }
    }
}
