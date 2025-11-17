package ims;

import common.Controller;
import common.Display;

/**
 * Console helper responsible for rendering login prompts and collecting credentials.
 */
public class LoginDisplay extends Display {
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
