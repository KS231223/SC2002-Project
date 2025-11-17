package ims;

import common.Controller;
import common.Display;

/**
 * Console helper that renders registration prompts and gathers user input.
 */
public class RegistrationDisplay extends Display {

    /**
     * Creates the display helper bound to the registration controller.
     *
     * @param owner controller coordinating this display
     */
    public RegistrationDisplay(Controller owner) {
        super(owner);
    }

    /** Prints the registration menu header. */
    @Override
    public void print_menu() {
        System.out.println("=== Company Representative Registration ===");
    }

    // Simple input collector
    /**
     * Reads a response from the console after printing the supplied prompt.
     *
     * @param prompt text displayed to the user before reading input
     * @return captured user input
     */
    public String getInput(String prompt) {
        System.out.print(prompt);
        return get_user_input();
    }
}
