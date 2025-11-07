package ims;

import common.*;
import cr.*;
import exceptions.*;
import java.io.*;
import java.util.*;
import staff.*;
import student.*;

public class LoginController extends Controller {
    private final Display loginDisplay;
    private static final String USER_DB_PATH =
        PathResolver.resource("users.csv");

    public LoginController(Router router, Scanner scanner) {
        super(router, scanner);
        this.loginDisplay = new LoginDisplay(this);
    }

    public void start() {
        router.push(this);
    }

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
                    switch (role) {
                        case "staff" -> {
                            new StaffHomePageController(router, scanner, username).open();
                        }
                        case "student" -> Objects.requireNonNull(new StudentHomePageController(router, scanner, username));
                        case "cr" -> Objects.requireNonNull(new CRHomePageController(router, scanner, username));
                        default -> System.err.println("Unknown role: " + role);
                    }
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
     * Get corresponding role for a username/password combo using DatabaseManager
     */
    private String get_corresponding_role(String username, String password) {
        File file = new File(USER_DB_PATH);
        if (!file.exists()) {
            System.out.println("User database not found.");
            return "None";
        }

        List<Entity> users = DatabaseManager.getDatabase(USER_DB_PATH, new ArrayList<>(), "User");

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

    private void destroy() {
        System.out.println("\nReturning to main menu...\n");
    }
}

class LoginDisplay extends Display {
    public LoginDisplay(Controller owner) {
        super(owner);
    }

    @Override
    public void print_menu() {
        System.out.println("=== Login ===");
    }

    public String[] get_credentials() {
        System.out.print("Username: ");
        String username = get_user_input();
        System.out.print("Password: ");
        String password = get_user_input();
        return new String[]{username, password};
    }
}
