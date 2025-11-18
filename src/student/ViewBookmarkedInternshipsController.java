package student;

import common.*;
import exceptions.*;
import java.util.*;

public class ViewBookmarkedInternshipsController extends StudentController {

    private ViewBookmarkedInternshipsDisplay display;
    private static final String BOOKMARKS_FILE =
        PathResolver.resource("bookmarked_internships.csv");
    private static final String INTERNSHIP_FILE =
        PathResolver.resource("internship_opportunities.csv");

    public ViewBookmarkedInternshipsController(Router router, Scanner scanner, EntityStore entityStore, String studentID) throws InvalidStudentIDException {
        super(router, scanner, entityStore, studentID);
        this.display = new ViewBookmarkedInternshipsDisplay(this);
        router.push(this);
    }

    @Override
    public void initialize() {
        List<Entity> bookmarks = entityStore.loadAll(BOOKMARKS_FILE, "Bookmark");
        List<Entity> allInternships = entityStore.loadAll(INTERNSHIP_FILE, "Internship");
        List<Entity> myBookmarkedInternships = new ArrayList<>();

        for (Entity bookmark : bookmarks) {
            if (bookmark.getArrayValueByIndex(0).equals(studentID)) {
                String internshipID = bookmark.getArrayValueByIndex(1);
                for (Entity internship : allInternships) {
                    if (internship.getArrayValueByIndex(0).equals(internshipID)) {
                        myBookmarkedInternships.add(internship);
                        break;
                    }
                }
            }
        }

        display.print_list(myBookmarkedInternships);
        System.out.println("\nPress Enter to return...");
        display.get_user_input();
        router.pop();
    }
}

class ViewBookmarkedInternshipsDisplay extends Display {

    public ViewBookmarkedInternshipsDisplay(Controller owner) {
        super(owner);
    }

    @Override
    public void print_menu() {}

    public void print_list(List<Entity> internships) {
        System.out.println("=== My Bookmarked Internships ===");
        if (internships.isEmpty()) {
            System.out.println("You have no bookmarked internships.");
            return;
        }
        for (int i = 0; i < internships.size(); i++) {
            System.out.println((i + 1) + ". " + internships.get(i).toString());
        }
    }
}
