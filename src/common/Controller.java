package common;

import java.util.Scanner;

/**
 * Base abstraction for interactive flows that participate in routing.
 */
public abstract class Controller {
    /** Shared input source for menu interactions. */
    public final Scanner scanner;
    /** Router controlling controller navigation. */
    public final Router router;

    /**
     * Builds a controller bound to the shared router and scanner.
     *
     * @param router navigation coordinator
     * @param scanner shared input stream
     */
    public Controller(Router router,Scanner scanner){
        this.router = router;
        this.scanner = scanner;
    }

    /**
     * Entry point invoked when the controller becomes active.
     */
    public abstract void initialize();
}
