package common;

import java.util.Stack;

//PUSH IS FOR ADDING A NEW SERVICE OR A PAGE. USED IF NEED TO BACKTRACK
//REPLACE IS FOR SWAPPING THE CURRENT ROLE OF THE APP TO A NEW ROLE LIKE FROM GUEST TO STAFF
//POP IS FOR EXITING THE CURRENT SERVICE OR PAGE AND BACKTRACKING.

/**
 * Manages a stack of controllers representing the current navigation history.
 */
public class Router {
    private final Stack<Controller> controllerStack;

    /**
     * Creates an empty router stack.
     */
    public Router() {
        controllerStack = new Stack<>();
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
    /**
     * Activates a new controller by pushing it on the stack and initializing it.
     */
    public void push(Controller controller) {
        if (controller == null) {
            throw new IllegalArgumentException("Controller cannot be null");
        }
        controllerStack.push(controller);
        System.out.println(controllerStack);
        controller.initialize();
        this.pop();
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
    /**
     * Check if the stack is empty.
     */
    public boolean isEmpty() {
        return controllerStack.isEmpty();
    }

    /**
     * Get the current stack size.
     */
    /**
     * Get the current stack size.
     */
    public int size() {
        return controllerStack.size();
    }
}
