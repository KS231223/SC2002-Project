package common;
import java.util.Scanner;

/**
 * Base class for console displays that coordinate user prompts for controllers.
 */
public abstract class Display {
    protected final Controller owner; // the object that will receive responses
    protected final Scanner scanner; // input scanner

    /**
     * Creates a display bound to the controller's shared scanner.
     *
     * @param owner controller that owns this display
     */
    public Display(Controller owner) {
        this.owner = owner;
        this.scanner = owner.scanner;
    }

    /**
     * Prints the display-specific menu to the console.
     */
    public abstract void print_menu();

    /**
     * Reads a line of user input, trimming surrounding whitespace.
     *
     * @return trimmed user input
     */
    public String get_user_input(){


            String input = scanner.nextLine().trim(); // use scanner from abstract class
            // Example of sending input to owner
            //System.out.println("Sending response to owner: " + input);
            //This is just for testing purposes
            // If owner has a method like owner.receiveResponse(input), you could call it here

            return input;
    }

    /**
     * Closes the shared scanner. Use with caution as this affects the owning controller.
     */
    public void close() {
        scanner.close();
    }
}
