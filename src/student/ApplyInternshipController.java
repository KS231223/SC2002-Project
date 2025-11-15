package student;

import common.*;
import exceptions.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Allows students to apply for available internship opportunities.
 */
public class ApplyInternshipController extends StudentController {

    private final ApplyInternshipDisplay display;
    private static final String INTERNSHIP_FILE =
        PathResolver.resource("internship_opportunities.csv");
    private static final String APPLICATION_FILE =
        PathResolver.resource("internship_applications.csv");
    private static final String STUDENT_FILE =
        PathResolver.resource("student.csv");

    /**
     * Creates a controller enabling applications to internships.
     *
     * @param router    router managing navigation
     * @param scanner   shared console input
     * @param studentID identifier for the logged-in student
     * @throws InvalidStudentIDException when {@code studentID} is invalid
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public ApplyInternshipController(Router router, Scanner scanner, String studentID) throws InvalidStudentIDException {
        super(router, scanner, studentID);
        this.display = new ApplyInternshipDisplay(this);
        router.push(this);
    }

    /**
     * Presents available internships, validates selections, and persists a new
     * application when eligibility and deadlines permit.
     */
    @Override
    public void initialize() {
        List<Entity> internships = DatabaseManager.getDatabase(INTERNSHIP_FILE, new ArrayList<>(), "Internship");
        if (internships.isEmpty()) {
            System.out.println("No internships available.");
            router.pop();
            return;
        }

        display.print_list(internships);
        String internshipId = display.ask_internship_id();

        Entity internship = DatabaseManager.getEntryById(INTERNSHIP_FILE, internshipId, "Internship");
        if (internship == null) {
            System.out.println("Invalid Internship ID. Returning...");
            router.pop();
            return;
        }

        // Create a new application
        String applicationId = UUID.randomUUID().toString();
        /* String appID, String studentID, String internshipID,
                String status, String submissionDate

        InternshipID, Title, Description, Level, PreferredMajor,
        OpenDate, CloseDate, Status, CompanyName, CRInCharge, Slots, Visibility

           StudentID, Name, Major, Year, Email, PreferredMajors, PreferredInternshipLevel, PreferredClosingDate, InternshipStatus, AcceptedInternshipID
                */
        String formattedDate = LocalDate.now().toString();
        LocalDate date1 = LocalDate.parse(formattedDate);
        LocalDate date2;
        try {
            date2 = LocalDate.parse(internship.getArrayValueByIndex(6), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            System.err.println("Invalid internship close date format.");
            router.pop();
            return;
        }
        Entity thisStudent = DatabaseManager.getEntryById(STUDENT_FILE,userID,"Student");
        if (thisStudent == null) {
            System.err.println("This student not found in database.");
            router.pop();
            return;
        }
        try {
            if (date1.isBefore(date2) && YearChecker.checkYear(thisStudent.getArrayValueByIndex(3), internship.getArrayValueByIndex(3))) {
                ApplicationEntity newApp = new ApplicationEntity(applicationId, userID, internshipId, "Pending", formattedDate);
                DatabaseManager.appendEntry(APPLICATION_FILE, newApp);

                System.out.println("Application submitted successfully!");
                router.pop();
            }
            else{
                System.out.println("Application Unsuccessful.");
                if(date1.isBefore(date2)) System.out.println("Unable to apply for non-basic courses at current year.");
                else System.out.println("Passed application deadline.");
            }
        }
        catch (Exception e){
            System.err.println("Invalid Application");
            router.pop();
        }

    }

    /**
     * Helper used to verify that a student meets the internship level
     * requirement.
     */
    private static class YearChecker {
        /**
         * Returns {@code true} when the student's year permits the selected
         * internship level.
         */
        static boolean checkYear(String studentsAge, String internshipLevel) {
            return !(("1".equals(studentsAge) || "2".equals(studentsAge))
                    && !"Basic".equalsIgnoreCase(internshipLevel));
        }
    }
}

/**
 * Display helper for the internship application flow.
 */
class ApplyInternshipDisplay extends Display {
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
    public void print_list(List<Entity> internships) {
        System.out.println("=== Available Internships ===");
        for (Entity e : internships) {
            System.out.println(e.toString());
        }
    }

    /**
     * Prompts the student for the internship identifier to apply to.
     *
     * @return provided internship identifier
     */
    public String ask_internship_id() {
        System.out.print("Enter Internship ID to apply: ");
        return get_user_input();
    }
}
