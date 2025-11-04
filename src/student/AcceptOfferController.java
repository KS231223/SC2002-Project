package student;

import common.*;
import exceptions.*;
import java.util.*;

public class AcceptOfferController extends StudentController {

    private AcceptOfferDisplay display;
    private static final String APPLICATION_FILE =
        PathResolver.resource("internship_applications.csv");

    public AcceptOfferController(Router router, Scanner scanner, String studentID) throws InvalidStudentIDException {
        super(router, scanner, studentID);
        this.display = new AcceptOfferDisplay(this);
        router.push(this);
    }

    @Override
    public void initialize() {
        StudentEntity thisStudent = StudentFilterService.loadStudent(studentID);
        if (thisStudent == null) {
            System.err.println("This student does not exist");
            router.pop();
            return;
        }
        List<Entity> applications = DatabaseManager.getDatabase(APPLICATION_FILE, new ArrayList<>(), "Application");
        List<Entity> offers = new ArrayList<>();

        for (Entity e : applications) {
            if (e.getArrayValueByIndex(1).equals(userID) && e.getArrayValueByIndex(3).equalsIgnoreCase("Approved")) {
                offers.add(e);
            }
        }

        if (offers.isEmpty()) {
            System.out.println("No internship offers available.");
            router.pop();
            return;
        }

        display.print_list(offers);
        String appId = display.ask_app_id();

        Entity offer = DatabaseManager.getEntryById(APPLICATION_FILE, appId, "Application");
        if (offer == null || !offer.getArrayValueByIndex(1).equals(userID)) {
            System.out.println("Invalid application ID.");
            router.pop();
            return;
        }

        // Update status
        offer.setArrayValueByIndex(3,"Accepted");
        DatabaseManager.updateEntry(APPLICATION_FILE, appId, offer, "Application");
        thisStudent.set(StudentEntity.StudentField.AcceptedInternshipID, offer.getArrayValueByIndex(2));
        StudentFilterService.saveStudent(thisStudent);
        this.acceptedInternshipID = offer.getArrayValueByIndex(2);
        System.out.println("Offer accepted successfully!");
        router.pop();
    }
}

class AcceptOfferDisplay extends Display {

    public AcceptOfferDisplay(Controller owner) {
        super(owner);
    }

    @Override
    public void print_menu() {}

    public void print_list(List<Entity> offers) {
        System.out.println("=== Internship Offers ===");
        for (Entity e : offers) {
            System.out.println(e.toString());
        }
    }

    public String ask_app_id() {
        System.out.print("Enter Application ID to accept: ");
        return get_user_input();
    }
}
