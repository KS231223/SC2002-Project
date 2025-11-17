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
    private ControllerFactory loginRegistry;

    /**
     * Creates a login controller bound to the shared router and input stream.
     *
     * @param router  navigation stack shared across controllers
     * @param scanner scanner used to capture user credentials
     */
    public LoginController(Router router, Scanner scanner, EntityStore entityStore) {
        super(router, scanner, entityStore);
        this.loginDisplay = new LoginDisplay(this);
        router.push(this);
//
    }
    protected ControllerFactory createRegistry(String userID) {
        return new LoginRegistry(router, scanner, entityStore, userID);
    }


    /**
     * Pushes this controller onto the router to begin the login workflow.
     */


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
                    loginRegistry = createRegistry(username);
                    // Set the user ID for the current session


                    // Delegate controller creation to the registry
                    loginRegistry.createController(role);

                } catch (IllegalArgumentException e) {
                    System.err.println("Unknown role: " + role);
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

/**
 * Console helper responsible for rendering login prompts and collecting credentials.
 */
class LoginDisplay extends Display {
    /**
     * Creates the display helper tied to the owning login controller.
     *
     * @param owner controller that uses this display
     */
    public LoginDisplay(Controller owner) {
        super(owner);
    }

    /** Prints the login menu header. */
    @Override
    public void print_menu() {
        System.out.println("=== Login ===");
    }

    /**
     * Collects a username and password pair from the console input.
     *
     * @return two-element array containing username and password
     */
    public String[] get_credentials() {
        System.out.print("Username: ");
        String username = get_user_input();
        System.out.print("Password: ");
        String password = get_user_input();
        return new String[]{username, password};
    }
}
