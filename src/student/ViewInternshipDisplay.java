package student;

import common.Controller;
import common.Display;
import common.InternshipEntity;
import common.StudentFilterService;
import common.StudentEntity;
import java.util.List;

public class ViewInternshipDisplay extends Display {

    public ViewInternshipDisplay(Controller owner) {
        super(owner);
    }

    @Override
    public void print_menu() {
        System.out.println("=== Available Internships ===");
    }

    public void print_menu(StudentFilterService.StudentFilters filters) {
        print_menu();
        if (filters.hasActiveFilters()) {
            System.out.println("Applied filters:");
            System.out.println("- Level: " + format(filters.level()));
            System.out.println("- Company: " + format(filters.company()));
            System.out.println("- Status: " + format(filters.status()));
            System.out.println("- Preferred Major: " + format(filters.major()));
            System.out.println("- Sort: " + filters.closingSortDisplay());
        } else {
            System.out.println("No filters applied. Sorted alphabetically by title.");
        }
    }

    public void print_list(List<InternshipEntity> internships) {
        for (InternshipEntity internship : internships) {
            System.out.println("----------------------------------------");
            System.out.println("ID: " + safe(internship.get(InternshipEntity.InternshipField.InternshipID)));
            System.out.println("Title: " + safe(internship.get(InternshipEntity.InternshipField.Title)));
            System.out.println("Company: " + safe(internship.get(InternshipEntity.InternshipField.CompanyName)));
            System.out.println("Level: " + safe(internship.get(InternshipEntity.InternshipField.Level)));
            System.out.println("Preferred Major: " + safe(internship.get(InternshipEntity.InternshipField.PreferredMajor)));
            System.out.println("Status: " + safe(internship.get(InternshipEntity.InternshipField.Status)));
            System.out.println("Open Date: " + safe(internship.get(InternshipEntity.InternshipField.OpenDate)));
            System.out.println("Close Date: " + safe(internship.get(InternshipEntity.InternshipField.CloseDate)));
            System.out.println("Slots: " + safe(internship.get(InternshipEntity.InternshipField.Slots)));
        }
        System.out.println("----------------------------------------");
    }

    private String safe(String value) {
        return (value == null || value.isBlank()) ? "Not specified" : value;
    }

    private String format(String value) {
        if (value == null || value.equalsIgnoreCase(StudentEntity.NO_FILTER_VALUE)) {
            return "None";
        }
        return value;
    }
}
