package student;

import common.*;
import exceptions.*;
import java.util.*;

/**
 * Presents internship listings filtered according to the student's saved
 * preferences.
 */
public class ViewInternshipController extends StudentController {

    private final ViewInternshipDisplay display;
    private static final String INTERNSHIP_FILE =
        PathResolver.resource("internship_opportunities.csv");

    /**
     * Creates a controller to display internships respecting student filters.
     *
     * @param router    router managing navigation
     * @param scanner   shared console input
     * @param studentID identifier for the logged-in student
     * @throws InvalidStudentIDException when {@code studentID} cannot be resolved
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public ViewInternshipController(Router router, Scanner scanner, String studentID) throws InvalidStudentIDException {
        super(router, scanner, studentID);
        this.display = new ViewInternshipDisplay(this);
        router.push(this);
    }

    /**
     * Loads internships, applies the student's filters, and displays matching
     * opportunities.
     */
    @Override
    public void initialize() {
        StudentEntity student = StudentFilterService.loadStudent(studentID);
        if (student == null) {
            System.out.println("Unable to load student profile. Returning...");
            router.pop();
            return;
        }

        List<Entity> internships = DatabaseManager.getDatabase(INTERNSHIP_FILE, new ArrayList<>(), "Internship");
        if (internships.isEmpty()) {
            System.out.println("No internships available at the moment.");
            router.pop();
            return;
        }

        StudentFilterService.StudentFilters filters = StudentFilterService.extractFilters(student);
        List<InternshipEntity> filteredInternships = new ArrayList<>();
        for (Entity entity : internships) {
            InternshipEntity internship = (InternshipEntity) entity;
            if (StudentFilterService.matchesFilters(internship, filters)) {
                filteredInternships.add(internship);
            }
        }

        if (filteredInternships.isEmpty()) {
            display.print_menu(filters);
            System.out.println("No internships match your current filters.");
            System.out.println("\nPress Enter to return...");
            display.get_user_input();
            router.pop();
            return;
        }

        StudentFilterService.sortInternships(filteredInternships, filters);
        display.print_menu(filters);
        display.print_list(filteredInternships);
        System.out.println("\nPress Enter to return...");
        display.get_user_input();
        router.pop();
    }
}

/**
 * Display helper for presenting internships and filter summaries.
 */
class ViewInternshipDisplay extends Display {

    /**
     * Creates a display bound to the internship listing controller.
     *
     * @param owner controller managing this display
     */
    public ViewInternshipDisplay(Controller owner) {
        super(owner);
    }

    /**
     * Prints the heading for the internships list.
     */
    @Override
    public void print_menu() {
        System.out.println("=== Available Internships ===");
    }

    /**
     * Prints the menu along with applied filter information.
     *
     * @param filters filters currently affecting the listings
     */
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

    /**
     * Renders the details of each internship in the provided list.
     *
     * @param internships internships to display
     */
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

    /**
     * Returns a safe string, substituting a default when the value is blank.
     */
    private String safe(String value) {
        return (value == null || value.isBlank()) ? "Not specified" : value;
    }

    /**
     * Formats filter values for display.
     */
    private String format(String value) {
        if (value == null || value.equalsIgnoreCase(StudentEntity.NO_FILTER_VALUE)) {
            return "None";
        }
        return value;
    }
}
