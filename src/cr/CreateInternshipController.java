package cr;

import common.*;
import exceptions.InvalidCompanyRepIDException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Guides company representatives through drafting a new internship submission.
 */
public class CreateInternshipController extends CRController {

    private static final String CR_FILE =
        PathResolver.resource("cr.csv");
    private static final String PENDING_INTERNSHIP_FILE =
        PathResolver.resource("pending_internship_opportunities.csv");
    private final CreateInternshipDisplay display;

    /**
     * Creates a controller for collecting internship submission details.
     *
     * @param router router managing navigation
     * @param scanner shared console input
     * @param crID identifier of the logged-in company representative
     * @throws InvalidCompanyRepIDException when {@code crID} cannot be resolved
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public CreateInternshipController(Router router, Scanner scanner, EntityStore entityStore, String crID) throws InvalidCompanyRepIDException {
        super(router, scanner, entityStore, crID);
        this.display = new CreateInternshipDisplay(this);
        router.push(this);
    }

    /**
     * Prompts the representative for internship details, performs validation,
     * and records the submission for staff review.
     */
    @Override
    public void initialize() {
        try {
            System.out.println("=== Create New Internship ===");
            String id = UUID.randomUUID().toString();
            String title = display.ask("Enter internship title: ");
            String desc = display.ask("Enter description: ");
            String level = display.ask("Enter level (Basic/Intermediate/Advanced): ").trim();
            String major = display.ask("Enter preferred major: ");
            String openDate = LocalDate.now().toString();
            String closeDateInput = display.ask("Enter closing date (YYYY-MM-DD): ");
            String status = "Pending";
            String slots = display.ask("Enter number of slots: ");
            String visibility = "Hidden"; // default hidden until approved

            // Validate internship level
            if (!level.equalsIgnoreCase("Basic") &&
                    !level.equalsIgnoreCase("Intermediate") &&
                    !level.equalsIgnoreCase("Advanced")) {
                System.out.println("Invalid internship opportunity: Level must be Basic, Intermediate, or Advanced.");
                router.pop();
                return;
            }

            // Validate close date format
            LocalDate closeDate;
            try {
                closeDate = LocalDate.parse(closeDateInput, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                if (closeDate.isBefore(LocalDate.now())) {
                    System.out.println("Invalid internship opportunity: Closing date cannot be in the past.");
                    router.pop();
                    return;
                }
            } catch (Exception e) {
                System.out.println("Invalid internship opportunity: Closing date format must be YYYY-MM-DD.");
                router.pop();
                return;
            }

            // Get company name from CR database
            Entity thisCR = entityStore.findById(CR_FILE, userID, "CR");
            if (thisCR == null) {
                System.out.println("Error: Company Representative record not found.");
                router.pop();
                return;
            }

            InternshipEntity newInternship = new InternshipEntity(id, title, desc, level, major,
                    openDate, closeDate.toString(), status, companyName, userID, slots, visibility);

            entityStore.append(PENDING_INTERNSHIP_FILE, newInternship);
            System.out.println("Internship successfully created!");
        } catch (Exception e) {
            System.err.println("Error creating internship: " + e.getMessage());
        } finally {
            router.pop();
        }
    }
}

// Create internship display moved to `cr.CreateInternshipDisplay`
