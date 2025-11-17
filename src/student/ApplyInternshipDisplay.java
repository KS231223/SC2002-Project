package student;

import common.Controller;
import common.Display;
import common.InternshipEntity;
import java.util.List;

public class ApplyInternshipDisplay extends Display {
    /**
     * Creates a display bound to the application controller.
     *
     * @param owner controller coordinating the display
     */
    public ApplyInternshipDisplay(Controller owner) {
        super(owner);
    }

    @Override
    public void print_menu() {}

    /**
     * Prints the internships available for application.
     *
     * @param internships internships pulled from storage
     */
    public void print_list(List<InternshipEntity> internships) {
        System.out.println("=== Available Internships ===");
        int index = 1;
        for (InternshipEntity internship : internships) {
            System.out.printf("%d) %s%n", index++, fallback(internship.get(InternshipEntity.InternshipField.Title)));
            System.out.printf("   ID: %s | Company: %s | Level: %s%n",
                fallback(internship.get(InternshipEntity.InternshipField.InternshipID)),
                fallback(internship.get(InternshipEntity.InternshipField.CompanyName)),
                fallback(internship.get(InternshipEntity.InternshipField.Level)));
            System.out.printf("   Major: %s | Slots: %s%n",
                fallback(internship.get(InternshipEntity.InternshipField.PreferredMajor), "N/A"),
                fallback(internship.get(InternshipEntity.InternshipField.Slots), "N/A"));
            System.out.printf("   Open: %s | Close: %s | Status: %s%n",
                fallback(internship.get(InternshipEntity.InternshipField.OpenDate), "N/A"),
                fallback(internship.get(InternshipEntity.InternshipField.CloseDate), "N/A"),
                fallback(internship.get(InternshipEntity.InternshipField.Status), "N/A"));
            String description = fallback(internship.get(InternshipEntity.InternshipField.Description));
            if (!description.isEmpty()) {
                System.out.println("   Description: " + description);
            }
            System.out.println();
        }
    }

    /**
     * Prompts the student for the internship identifier to apply to.
     *
     * @return provided internship identifier
     */
    public String ask_internship_id() {
        System.out.print("Enter Internship ID to apply (or B to go back): ");
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
