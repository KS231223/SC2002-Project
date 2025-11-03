package common;
import java.util.Scanner;
import exceptions.*;

public abstract class Display {
    protected final Controller owner; // the object that will receive responses
    protected final Scanner scanner; // input scanner

    // Constructor for the abstract class
    public Display(Controller owner) {
        this.owner = owner;
        this.scanner = owner.scanner;
    }

    // Abstract methods that subclasses must implement
    public abstract void print_menu();
    public String get_user_input(){


            String input = scanner.nextLine().trim(); // use scanner from abstract class
            // Example of sending input to owner
            //System.out.println("Sending response to owner: " + input);
            //This is just for testing purposes
            // If owner has a method like owner.receiveResponse(input), you could call it here

            return input;
    }

    // Optional: a method to close the scanner
    public void close() {
        scanner.close();
    }
}
