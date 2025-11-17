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
    public ViewInternshipController(Router router, Scanner scanner, EntityStore entityStore, String studentID) throws InvalidStudentIDException {
        super(router, scanner, entityStore, studentID);
        this.display = new ViewInternshipDisplay(this);
        router.push(this);
    }

    /**
     * Loads internships, applies the student's filters, and displays matching
     * opportunities.
     */
    @Override
    public void initialize() {
        StudentEntity student = StudentFilterService.loadStudent(entityStore, studentID);
        if (student == null) {
            System.out.println("Unable to load student profile. Returning...");
            router.pop();
            return;
        }

        List<Entity> internships = entityStore.loadAll(INTERNSHIP_FILE, "Internship");
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

// Internship display moved to `student.ViewInternshipDisplay`
