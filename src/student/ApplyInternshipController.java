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
    public ApplyInternshipController(Router router, Scanner scanner, EntityStore entityStore, String studentID) throws InvalidStudentIDException {
        super(router, scanner, entityStore, studentID);
        this.display = new ApplyInternshipDisplay(this);
        router.push(this);
    }

    /**
     * Presents available internships, validates selections, and persists a new
     * application when eligibility and deadlines permit.
     */
    @Override
    public void initialize() {
        List<Entity> internships = entityStore.loadAll(INTERNSHIP_FILE, "Internship");
        List<InternshipEntity> visibleInternships = new ArrayList<>();
        for (Entity entity : internships) {
            InternshipEntity internship = (InternshipEntity) entity;
            if (isVisible(internship)) {
                visibleInternships.add(internship);
            }
        }

        if (visibleInternships.isEmpty()) {
            System.out.println("No internships available.");
            router.pop();
            return;
        }

        display.print_list(visibleInternships);
        String internshipId = display.ask_internship_id();
        if (internshipId == null) {
            System.out.println("No input captured. Returning...");
            router.pop();
            return;
        }

        String trimmedId = internshipId.trim();
        if ("b".equalsIgnoreCase(trimmedId)) {
            router.pop();
            return;
        }

        Entity internship = entityStore.findById(INTERNSHIP_FILE, trimmedId, "Internship");
        InternshipEntity selectedInternship = (InternshipEntity) internship;
        if (selectedInternship == null || !isVisible(selectedInternship)) {
            System.out.println("Invalid or unavailable Internship ID. Returning...");
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
            date2 = LocalDate.parse(selectedInternship.get(InternshipEntity.InternshipField.CloseDate), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            System.err.println("Invalid internship close date format.");
            router.pop();
            return;
        }
        Entity thisStudent = entityStore.findById(STUDENT_FILE, userID, "Student");
        if (thisStudent == null) {
            System.err.println("This student not found in database.");
            router.pop();
            return;
        }
        try {
            String studentYear = optionalTrim(thisStudent.getArrayValueByIndex(3));
            String internshipLevel = optionalTrim(selectedInternship.get(InternshipEntity.InternshipField.Level));
            boolean beforeDeadline = date1.isBefore(date2);
            boolean eligibleByYear = YearChecker.checkYear(studentYear, internshipLevel);

            if (beforeDeadline && eligibleByYear) {
                ApplicationEntity newApp = new ApplicationEntity(applicationId, userID, trimmedId, "Pending", formattedDate);
                entityStore.append(APPLICATION_FILE, newApp);

                System.out.println("Application submitted successfully!");
                router.pop();
            }
            else{
                System.out.println("Application Unsuccessful.");
                if (!beforeDeadline) {
                    System.out.println("Passed application deadline.");
                } else {
                    String displayLevel = internshipLevel.isEmpty() ? "this internship" : "the " + internshipLevel + " internship";
                    if (studentYear.isEmpty()) {
                        System.out.printf("Your current year allows applications only to Basic internships; %s requires a higher level.%n",
                            displayLevel);
                    } else {
                        System.out.printf("Students in Year %s may only apply to Basic internships; %s requires a higher level.%n",
                            studentYear, displayLevel);
                    }
                }
            }
        }
        catch (Exception e){
            System.err.println("Invalid Application");
            router.pop();
        }

    }

    private static boolean isVisible(InternshipEntity internship) {
        if (internship == null) {
            return false;
        }
        String visibility = internship.get(InternshipEntity.InternshipField.Visibility);
        return visibility == null || !"Hidden".equalsIgnoreCase(visibility.trim());
    }

    private static String optionalTrim(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
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
