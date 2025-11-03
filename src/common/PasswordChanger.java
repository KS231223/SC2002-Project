package common;

import java.io.*;
import java.util.*;
import exceptions.*;

public class PasswordChanger extends Controller {

    private  String username;
    protected Display display; // protected, non-static

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
        } catch (Exception e) {
            System.err.println("Error updating password: " + e.getMessage());
        }
        finally {
            router.pop();
        }
    }

    // Backend logic for changing password
    private void changePassword(String newPassword) throws IOException, InvalidUserIDException {
        File file = new File("resources/users.csv");
        if (!file.exists()) {
            throw new IOException("User database not found.");
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
        }

        if (!found) {
            throw new InvalidUserIDException("User not found in database: " + username);
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (String line : lines) {
                writer.println(line);
            }
        }
    }

}
class ChangePasswordDisplay extends Display {

    ChangePasswordDisplay(Controller owner) {
        super(owner);
    }

    @Override
    public void print_menu() {
        System.out.println("=== Change Password ===");
    }

    String getNewPassword() {
        System.out.print("Enter new password: ");
        return get_user_input();
    }
}
