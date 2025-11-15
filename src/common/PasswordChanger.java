package common;

import exceptions.InvalidUserIDException;
import java.io.*;
import java.util.*;

/**
 * Handles the change-password flow for any authenticated user.
 */
@SuppressWarnings("LeakingThisInConstructor")
public class PasswordChanger extends Controller {

    private final String username;
    protected final Display display; // protected, non-static

    /**
     * Creates a password change controller and immediately schedules it with the router.
     */
    public PasswordChanger(Router router,Scanner scanner, String username) {
        super(router,scanner);
        this.username = username;
        this.display = new ChangePasswordDisplay(this); // tied to this instance
        router.push(this);
    }

    @Override
    public void initialize() {
        display.print_menu();

        // Collect input from display
        String newPassword = ((ChangePasswordDisplay) display).getNewPassword();

        // Controller handles all logic
        try {
            changePassword(newPassword);
            System.out.println("Password updated successfully for user: " + username);
        } catch (InvalidUserIDException e) {
            System.err.println("Error updating password: " + e.getMessage());
        }
        finally {
            router.pop();
        }
    }

    // Backend logic for changing password
    /**
     * Persists a new password for the current user in {@code users.csv}.
     */
    private void changePassword(String newPassword) throws InvalidUserIDException {
        File file = new File(PathResolver.resource("users.csv"));
        if (!file.exists()) {
            throw new InvalidUserIDException("User database not found.");
        }

        List<String> lines = new ArrayList<>();
        boolean found = false;

        try (Scanner reader = new Scanner(file)) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                String[] parts = line.split(",");
                if (parts.length < 2) {
                    lines.add(line);
                    continue;
                }

                if (parts[0].equals(username)) {
                    parts[1] = newPassword;
                    found = true;
                }

                lines.add(String.join(",", parts));
            }
        } catch (FileNotFoundException e) {
            throw new InvalidUserIDException("User database not accessible: " + e.getMessage());
        }

        if (!found) {
            throw new InvalidUserIDException("User not found in database: " + username);
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (String line : lines) {
                writer.println(line);
            }
        } catch (IOException e) {
            throw new InvalidUserIDException("Unable to persist password update: " + e.getMessage());
        }
    }

}

/**
 * Console display supporting the change-password workflow.
 */
class ChangePasswordDisplay extends Display {

    ChangePasswordDisplay(Controller owner) {
        super(owner);
    }

    @Override
    public void print_menu() {
        System.out.println("=== Change Password ===");
    }

    /**
     * Prompts the user for the replacement password.
     */
    String getNewPassword() {
        System.out.print("Enter new password: ");
        return get_user_input();
    }
}
