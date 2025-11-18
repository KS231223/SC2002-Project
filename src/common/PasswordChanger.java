package common;

import exceptions.InvalidUserIDException;
import java.util.Scanner;

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
    public PasswordChanger(Router router, Scanner scanner, EntityStore entityStore, String username) {
        super(router, scanner, entityStore);
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
        Entity entity = entityStore.findById(PathResolver.resource("users.csv"), username, "User");
        if (!(entity instanceof UserEntity user)) {
            throw new InvalidUserIDException("User not found in database: " + username);
        }

        user.set(UserEntity.UserField.Password, newPassword);
        entityStore.update(PathResolver.resource("users.csv"), username, user, "User");
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
