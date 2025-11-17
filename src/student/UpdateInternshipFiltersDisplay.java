package student;

import common.Controller;
import common.Display;
import common.StudentEntity;
import common.StudentFilterService;
import java.util.List;

public class UpdateInternshipFiltersDisplay extends Display {

    UpdateInternshipFiltersDisplay(Controller owner) {
        super(owner);
    }

    @Override
    public void print_menu() {
        // Menu rendering handled via printMenu(StudentEntity) to include filter context.
    }

    public void printMenu(StudentEntity student) {
        StudentFilterService.StudentFilters filters = StudentFilterService.extractFilters(student);
        System.out.println("=== Update Internship Filters ===");
        System.out.println("Current selections:");
        System.out.println("- Level: " + format(filters.level()));
        System.out.println("- Company: " + format(filters.company()));
        System.out.println("- Status: " + format(filters.status()));
        System.out.println("- Preferred Major: " + format(filters.major()));
        System.out.println("- Sort: " + filters.closingSortDisplay());
        System.out.println();
        System.out.println("Choose a filter to update:");
        System.out.println("1. Internship Level");
        System.out.println("2. Company Name");
        System.out.println("3. Status");
        System.out.println("4. Preferred Major");
        System.out.println("5. Closing Date Sort");
        System.out.println("6. Save and return");
        System.out.print("Select an option: ");
    }

    public void printLevelOptions() {
        System.out.println("Select level filter:");
        System.out.println("1. None");
        System.out.println("2. Basic");
        System.out.println("3. Intermediate");
        System.out.println("4. Advanced");
        System.out.print("Choice: ");
    }

    public void printCompanyOptions(List<String> companies) {
        System.out.println("Select company filter (enter number or type a company name):");
        System.out.println("0. None");
        for (int i = 0; i < companies.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, companies.get(i));
        }
        System.out.print("Choice: ");
    }

    public void printStatusOptions() {
        System.out.println("Select internship status:");
        System.out.println("1. None");
        System.out.println("2. Approved");
        System.out.println("3. Pending");
        System.out.println("4. Rejected");
        System.out.println("5. Filled");
        System.out.print("Choice: ");
    }

    public void promptForMajor() {
        System.out.print("Enter preferred major (leave blank for none): ");
    }

    public void printClosingOptions() {
        System.out.println("Select closing date preference:");
        System.out.println("1. Alphabetical by title (default)");
        System.out.println("2. Soonest closing first");
        System.out.println("3. Latest closing first");
        System.out.print("Choice: ");
    }

    private String format(String value) {
        if (value == null || value.equalsIgnoreCase(StudentEntity.NO_FILTER_VALUE)) {
            return "None";
        }
        return value;
    }
}
