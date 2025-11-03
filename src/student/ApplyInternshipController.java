package student;

import common.*;
import ims.*;
import exceptions.*;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ApplyInternshipController extends StudentController {

    private ApplyInternshipDisplay display;
    private static final String INTERNSHIP_FILE = "resources/internship_opportunities.csv";
    private static final String APPLICATION_FILE = "resources/internship_applications.csv";
    private static final String STUDENT_FILE = "resources/student.csv";

    public ApplyInternshipController(Router router, Scanner scanner, String studentID) throws InvalidStudentIDException {
        super(router, scanner, studentID);
        this.display = new ApplyInternshipDisplay(this);
        router.push(this);
    }

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

    private static class YearChecker {
        // Returns true if the student is eligible for the internship
        static boolean checkYear(String studentsAge, String internshipLevel) {
            if ((studentsAge.equals("1") || studentsAge.equals("2")) && !internshipLevel.equalsIgnoreCase("Basic")) {
                return false;
            }
            return true;
        }
    }
}

class ApplyInternshipDisplay extends Display {
    public ApplyInternshipDisplay(Controller owner) {
        super(owner);
    }

    @Override
    public void print_menu() {}

    public void print_list(List<Entity> internships) {
        System.out.println("=== Available Internships ===");
        for (Entity e : internships) {
            System.out.println(e.toString());
        }
    }

    public String ask_internship_id() {
        System.out.print("Enter Internship ID to apply: ");
        return get_user_input();
    }
}
