package student;

import common.Controller;
import common.Display;
import common.ApplicationEntity;
import common.Entity;
import java.util.List;

/**
 * Display helper for the withdrawal request flow.
 */
public class WithdrawalDisplay extends Display {
    /**
     * Creates a display bound to the withdrawal controller.
     *
     * @param owner controller managing this display
     */
    public WithdrawalDisplay(Controller owner) {
        super(owner);
    }

    @Override
    public void print_menu() {}

    /**
     * Prints the student's applications, allowing a withdrawal choice.
     *
     * @param apps applications available to withdraw from
     */
    public void print_list(List<Entity> apps) {
        System.out.println("=== My Applications ===");
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

    /**
     * Prompts the student for the application identifier to withdraw.
     *
     * @return user-provided application identifier
     */
    public String ask_app_id() {
        System.out.print("Enter Application ID to withdraw (or B to go back): ");
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
