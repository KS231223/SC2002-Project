package common;

import java.util.Stack;
import exceptions.*;

//PUSH IS FOR ADDING A NEW SERVICE OR A PAGE. USED IF NEED TO BACKTRACK
//REPLACE IS FOR SWAPPING THE CURRENT ROLE OF THE APP TO A NEW ROLE LIKE FROM GUEST TO STAFF
//POP IS FOR EXITING THE CURRENT SERVICE OR PAGE AND BACKTRACKING.

public class Router {
    private Stack<Controller> controllerStack;

    public Router() {
        controllerStack = new Stack<Controller>();
    }

    /**
     * Push a new controller onto the stack and initialize it.
     */
    public void replace(Controller controller){
        if (controller == null) {
            throw new IllegalArgumentException("Controller cannot be null");
        }

        controllerStack.pop();
        this.push(controller);

    }
    public void push(Controller controller) {
        if (controller == null) {
            throw new IllegalArgumentException("Controller cannot be null");
        }
        controllerStack.push(controller);
        System.out.println(controllerStack);
        controller.initialize();
    }

    /**
     * Pop the current controller and re-initialize the new top one (if any).
     */
    public Controller pop() {
        if (controllerStack.isEmpty()) {
            System.out.println("Router stack is empty, nothing to pop.");
            return null;
        }

        // Remove the current controller
        controllerStack.pop();

        // Re-initialize the new top controller if it exists
        if (!controllerStack.isEmpty()) {
            System.out.println(controllerStack);
            Controller top = controllerStack.peek();
            top.initialize();
            return top;
        }
        else{
            System.out.println("Thank you for using our IMS!");
            System.exit(0);
        }

        return null;
    }

    /**
     * Peek the current (top) controller without modifying the stack.
     */
    public Controller peek() {
        if (controllerStack.isEmpty()) {
            return null;
        }
        return controllerStack.peek();
    }

    /**
     * Check if the stack is empty.
     */
    public boolean isEmpty() {
        return controllerStack.isEmpty();
    }

    /**
     * Get the current stack size.
     */
    public int size() {
        return controllerStack.size();
    }
}
