package student;

import common.*;
import exceptions.*;
import java.util.*;

/**
 * Handles student acceptance of approved internship offers.
 */
public class AcceptOfferController extends StudentController {

    private final AcceptOfferDisplay display;
    private static final String APPLICATION_FILE =
        PathResolver.resource("internship_applications.csv");

    /**
     * Creates a controller that lets a student accept internship offers.
     *
     * @param router    router managing navigation
     * @param scanner   shared console input
     * @param studentID identifier for the logged-in student
     * @throws InvalidStudentIDException when {@code studentID} cannot be resolved
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public AcceptOfferController(Router router, Scanner scanner, EntityStore entityStore, String studentID) throws InvalidStudentIDException {
        super(router, scanner, entityStore, studentID);
        this.display = new AcceptOfferDisplay(this);
        router.push(this);
    }

    /**
     * Loads approved offers for the current student and records the acceptance
     * of the selected offer.
     */
    @Override
    public void initialize() {
        StudentEntity thisStudent = StudentFilterService.loadStudent(entityStore, studentID);
        if (thisStudent == null) {
            System.err.println("This student does not exist");
            router.pop();
            return;
        }
        List<Entity> applications = entityStore.loadAll(APPLICATION_FILE, "Application");
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
        if (appId == null) {
            System.out.println("No input captured. Returning...");
            router.pop();
            return;
        }

        String trimmedId = appId.trim();
        if ("b".equalsIgnoreCase(trimmedId)) {
            router.pop();
            return;
        }

        Entity offer = entityStore.findById(APPLICATION_FILE, trimmedId, "Application");
        if (offer == null || !offer.getArrayValueByIndex(1).equals(userID)) {
            System.out.println("Invalid application ID.");
            router.pop();
            return;
        }

        // Update status
        offer.setArrayValueByIndex(3,"Accepted");
        entityStore.update(APPLICATION_FILE, trimmedId, offer, "Application");
        thisStudent.set(StudentEntity.StudentField.AcceptedInternshipID, offer.getArrayValueByIndex(2));
        StudentFilterService.saveStudent(entityStore, thisStudent);
        this.acceptedInternshipID = offer.getArrayValueByIndex(2);
        System.out.println("Offer accepted successfully!");
        router.pop();
    }
}

// Accept offer display moved to `student.AcceptOfferDisplay`
