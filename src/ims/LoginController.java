package ims;

import common.*;
import cr.*;
import exceptions.*;
import java.util.*;
import staff.*;
import student.*;

/**
 * Coordinates credential collection and routes users to the appropriate home page based on their role.
 */
public class LoginController extends Controller {
    private final Display loginDisplay;
    private static final String USER_DB_PATH =
        PathResolver.resource("users.csv");

    /**
     * Creates a login controller bound to the shared router and input stream.
     *
     * @param router  navigation stack shared across controllers
     * @param scanner scanner used to capture user credentials
     */
    private final HomeControllerFactory homeFactory;

    public LoginController(Router router, Scanner scanner, EntityStore entityStore) {
        this(router, scanner, entityStore, new DefaultHomeControllerFactory());
    }

    public LoginController(Router router, Scanner scanner, EntityStore entityStore, HomeControllerFactory homeFactory) {
        super(router, scanner, entityStore);
        this.loginDisplay = new LoginDisplay(this);
        this.homeFactory = homeFactory;
    }

    /**
     * Pushes this controller onto the router to begin the login workflow.
     */
    public void start() {
        router.push(this);
    }

    /**
     * Handles the interactive login flow and routes the authenticated user to their landing page.
     */
    @Override
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void initialize() {
        try {
            loginDisplay.print_menu();
            String[] credentials = ((LoginDisplay) loginDisplay).get_credentials();

            String username = credentials[0];
            String password = credentials[1];

            String role = get_corresponding_role(username, password);

            if (!Objects.equals(role, "None")) {
                System.out.println("\nLogin successful! Welcome, " + username + "!");
                role = role.trim().toLowerCase();

                try {
                    homeFactory.navigateToHome(role, router, scanner, entityStore, username);
                } catch (InvalidStaffIDException | InvalidStudentIDException | InvalidCompanyRepIDException e) {
                    System.err.println("Error loading home page: " + e.getMessage());
                }
            } else {
                System.out.println("Login failed. Invalid username or password.");
            }

        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
            destroy();
        }
    }

    /**
     * Retrieves the role associated with the supplied credentials by querying the user database.
     *
     * @param username login identifier entered by the user
     * @param password password entered by the user
     * @return matching role string or {@code "None"} when the credentials are invalid
     */
    private String get_corresponding_role(String username, String password) {
        List<Entity> users = entityStore.loadAll(USER_DB_PATH, "User");

        for (Entity e : users) {
            if (!(e instanceof UserEntity user)) continue;

            String fileUsername = user.get(UserEntity.UserField.Username);
            String filePassword = user.get(UserEntity.UserField.Password);
            String fileRole = user.get(UserEntity.UserField.Role);

            if (fileUsername.equals(username) && filePassword.equals(password)) {
                System.out.println("Logging in...");
                return fileRole;
            }
        }

        System.out.println("Invalid credentials. Try again.");
        return "None";
    }

    /**
     * Returns control to the authentication menu.
     */
    private void destroy() {
        System.out.println("\nReturning to main menu...\n");
    }
}

// Login display moved to `ims.LoginDisplay`
