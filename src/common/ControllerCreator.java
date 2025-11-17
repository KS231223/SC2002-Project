package common;

/**
 * Functional factory used to create controllers for menu options.
 */
@FunctionalInterface
public interface ControllerCreator {
    Controller create() throws Exception;
}
