package common;

/**
 * Factory abstraction that maps an input token (menu option, role, etc.)
 * to the corresponding controller instance.
 */
public interface ControllerFactory {

    /**
     * Builds and optionally activates the controller bound to the supplied key.
     *
     * @param key lookup token, typically a menu selection or user role
     * @throws IllegalArgumentException if the key is unmapped by an implementation
     */
    void createController(String key);
}