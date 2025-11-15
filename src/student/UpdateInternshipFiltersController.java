package student;

import common.*;
import exceptions.InvalidStudentIDException;
import java.util.List;
import java.util.Scanner;

/**
 * Provides an interface for students to configure internship listing filters.
 */
public class UpdateInternshipFiltersController extends StudentController {

    private final UpdateInternshipFiltersDisplay display;
    private StudentEntity studentEntity;

    /**
     * Creates a controller that guides students through filter adjustments.
     *
     * @param router    router managing navigation
     * @param scanner   shared console input
     * @param studentID identifier for the logged-in student
     * @throws InvalidStudentIDException when {@code studentID} cannot be located
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public UpdateInternshipFiltersController(Router router, Scanner scanner, String studentID) throws InvalidStudentIDException {
        super(router, scanner, studentID);
        this.display = new UpdateInternshipFiltersDisplay(this);
        router.push(this);
    }

    /**
     * Loads existing filter preferences, presents the interactive menu, and
     * persists updates upon exit.
     */
    @Override
    public void initialize() {
        studentEntity = StudentFilterService.loadStudent(studentID);
        if (studentEntity == null) {
            System.out.println("Unable to load student filters. Returning to menu.");
            router.pop();
            return;
        }

        boolean running = true;
        while (running) {
            display.printMenu(studentEntity);
            String choice = display.get_user_input();
            switch (choice) {
                case "1" -> updateLevel();
                case "2" -> updateCompany();
                case "3" -> updateStatus();
                case "4" -> updateMajor();
                case "5" -> updateClosingSort();
                case "6" -> {
                    StudentFilterService.saveStudent(studentEntity);
                    System.out.println("Filters saved. Returning to menu...");
                    router.pop();
                    running = false;
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    /**
     * Updates the preferred internship level filter based on user input.
     */
    private void updateLevel() {
        display.printLevelOptions();
        String choice = display.get_user_input();
        switch (choice) {
            case "1" -> studentEntity.set(StudentEntity.StudentField.FilterLevel, StudentEntity.NO_FILTER_VALUE);
            case "2" -> studentEntity.set(StudentEntity.StudentField.FilterLevel, "Basic");
            case "3" -> studentEntity.set(StudentEntity.StudentField.FilterLevel, "Intermediate");
            case "4" -> studentEntity.set(StudentEntity.StudentField.FilterLevel, "Advanced");
            default -> {
                System.out.println("No changes made to level filter.");
                return;
            }
        }
        System.out.println("Level filter updated to: " + studentEntity.get(StudentEntity.StudentField.FilterLevel));
    }

    /**
     * Updates the preferred company filter based on user selection or input.
     */
    private void updateCompany() {
        List<String> companies = StudentFilterService.listCompanies();
        display.printCompanyOptions(companies);
        String choice = display.get_user_input();
        String newValue;
        if ("0".equals(choice)) {
            newValue = StudentEntity.NO_FILTER_VALUE;
        } else {
            try {
                int index = Integer.parseInt(choice);
                if (index >= 1 && index <= companies.size()) {
                    newValue = companies.get(index - 1);
                } else {
                    System.out.println("Invalid selection. No changes made to company filter.");
                    return;
                }
            } catch (NumberFormatException ex) {
                newValue = choice.trim();
                if (newValue.isEmpty()) {
                    newValue = StudentEntity.NO_FILTER_VALUE;
                } else if (newValue.equalsIgnoreCase(StudentEntity.NO_FILTER_VALUE)) {
                    newValue = StudentEntity.NO_FILTER_VALUE;
                }
            }
        }
        studentEntity.set(StudentEntity.StudentField.FilterCompany, newValue);
        System.out.println("Company filter updated to: " + studentEntity.get(StudentEntity.StudentField.FilterCompany));
    }

    /**
     * Updates the status filter reflecting internship approval state.
     */
    private void updateStatus() {
        display.printStatusOptions();
        String choice = display.get_user_input();
        switch (choice) {
            case "1" -> studentEntity.set(StudentEntity.StudentField.FilterStatus, StudentEntity.NO_FILTER_VALUE);
            case "2" -> studentEntity.set(StudentEntity.StudentField.FilterStatus, "Approved");
            case "3" -> studentEntity.set(StudentEntity.StudentField.FilterStatus, "Pending");
            case "4" -> studentEntity.set(StudentEntity.StudentField.FilterStatus, "Rejected");
            case "5" -> studentEntity.set(StudentEntity.StudentField.FilterStatus, "Filled");
            default -> {
                System.out.println("No changes made to status filter.");
                return;
            }
        }
        System.out.println("Status filter updated to: " + studentEntity.get(StudentEntity.StudentField.FilterStatus));
    }

    /**
     * Updates the preferred major filter stored on the student record.
     */
    private void updateMajor() {
        display.promptForMajor();
        String input = display.get_user_input().trim();
        String newValue = input.isEmpty() ? StudentEntity.NO_FILTER_VALUE : input;
        if (!input.isEmpty() && input.equalsIgnoreCase(StudentEntity.NO_FILTER_VALUE)) {
            newValue = StudentEntity.NO_FILTER_VALUE;
        }
        studentEntity.set(StudentEntity.StudentField.FilterMajor, newValue);
        System.out.println("Preferred major filter updated to: " + studentEntity.get(StudentEntity.StudentField.FilterMajor));
    }

    /**
     * Updates the closing date sort preference for internship listings.
     */
    private void updateClosingSort() {
        display.printClosingOptions();
        String choice = display.get_user_input();
        switch (choice) {
            case "1" -> studentEntity.set(StudentEntity.StudentField.FilterClosingSort, StudentEntity.NO_FILTER_VALUE);
            case "2" -> studentEntity.set(StudentEntity.StudentField.FilterClosingSort, StudentFilterService.SORT_SOONEST);
            case "3" -> studentEntity.set(StudentEntity.StudentField.FilterClosingSort, StudentFilterService.SORT_LATEST);
            default -> {
                System.out.println("No changes made to closing date preference.");
                return;
            }
        }
        System.out.println("Closing date preference set to: " +
                StudentFilterService.extractFilters(studentEntity).closingSortDisplay());
    }
}

/**
 * Display helper for presenting and editing student filter preferences.
 */
class UpdateInternshipFiltersDisplay extends Display {

    /**
     * Creates a display associated with the filter update controller.
     *
     * @param owner controller coordinating the display
     */
    UpdateInternshipFiltersDisplay(Controller owner) {
        super(owner);
    }

    @Override
    public void print_menu() {
        // Menu rendering handled via printMenu(StudentEntity) to include filter context.
    }

    /**
     * Renders the filter overview and main selection menu.
     *
     * @param student student whose filters are being edited
     */
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

    /**
     * Presents internship level options.
     */
    public void printLevelOptions() {
        System.out.println("Select level filter:");
        System.out.println("1. None");
        System.out.println("2. Basic");
        System.out.println("3. Intermediate");
        System.out.println("4. Advanced");
        System.out.print("Choice: ");
    }

    /**
     * Lists company choices and accepts user input.
     *
     * @param companies known companies from the dataset
     */
    public void printCompanyOptions(List<String> companies) {
        System.out.println("Select company filter (enter number or type a company name):");
        System.out.println("0. None");
        for (int i = 0; i < companies.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, companies.get(i));
        }
        System.out.print("Choice: ");
    }

    /**
     * Presents internship status filter options.
     */
    public void printStatusOptions() {
        System.out.println("Select internship status:");
        System.out.println("1. None");
        System.out.println("2. Approved");
        System.out.println("3. Pending");
        System.out.println("4. Rejected");
        System.out.println("5. Filled");
        System.out.print("Choice: ");
    }

    /**
     * Prompts for a preferred major value.
     */
    public void promptForMajor() {
        System.out.print("Enter preferred major (leave blank for none): ");
    }

    /**
     * Presents closing date sort preferences.
     */
    public void printClosingOptions() {
        System.out.println("Select closing date preference:");
        System.out.println("1. Alphabetical by title (default)");
        System.out.println("2. Soonest closing first");
        System.out.println("3. Latest closing first");
        System.out.print("Choice: ");
    }

    /**
     * Formats stored filter values for presentation.
     */
    private String format(String value) {
        if (value == null || value.equalsIgnoreCase(StudentEntity.NO_FILTER_VALUE)) {
            return "None";
        }
        return value;
    }
}
