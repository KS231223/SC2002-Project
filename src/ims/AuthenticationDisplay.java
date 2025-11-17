package ims;

import common.Controller;
import common.Display;

/**
 * Display for the authentication menu.
 */
public class AuthenticationDisplay extends Display {

    /**
     * Creates a display helper that renders the authentication menu.
     *
     * @param owner controller coordinating this display
     */
    public AuthenticationDisplay(Controller owner) {
        super(owner);
    }

    /** Prints the authentication menu options. */
    @Override
    public void print_menu() {
        System.out.println("=== Internship Placement Management System ===");
        System.out.println("1. Login");
        System.out.println("2. Register as Company Representative");
        System.out.println("3. Exit");
        System.out.print("Enter your choice: ");
    }
}
