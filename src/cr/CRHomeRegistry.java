package cr;
import common.*;
import exceptions.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Registry that maps company representative home-page menu choices to the
 * appropriate controller constructors.
 */
public class CRHomeRegistry implements ControllerFactory {

    /**
     * Mapping of option key to controller supplier.
     */
    public Map<String, ControllerSupplier> keyMap = new HashMap<>();

    /**
     * Builds the registry for a specific CR session.
     *
     * @param router  navigation router used to push controllers
     * @param scanner shared scanner for interactive input
     * @param store   entity store for persistence operations
     * @param userID  company representative identifier
     */
    public CRHomeRegistry(Router router, Scanner scanner, EntityStore store, String userID) {
        keyMap.put("1", () -> new ListMyInternshipsController(router, scanner, store, userID));
        keyMap.put("2", () -> new CreateInternshipController(router, scanner, store, userID));
        keyMap.put("3", () -> new ToggleVisibilityController(router, scanner, store, userID));
        keyMap.put("4", () -> new ViewApplicationsController(router, scanner, store, userID));
        keyMap.put("5", () -> new ReviewApplicationController(router, scanner, store, userID));
        keyMap.put("6", () -> new FilterInternshipsController(router, scanner, store, userID));
        keyMap.put("8", () -> new PasswordChanger(router, scanner, store, userID));
    }

    @Override
    /**
     * Instantiates the controller paired with the provided CR menu key.
     *
     * @param key action selected on the CR home screen
     * @throws IllegalArgumentException when no controller is mapped to the key
     */
    public void createController(String key){
        ControllerSupplier supplier = keyMap.get(key);
        if (supplier == null) {
            throw new IllegalArgumentException("No controller mapped for key: " + key);
        }

        try {
            Controller controller = supplier.create();
            // controller constructor can auto-register with router
        } catch (InvalidCompanyRepIDException e) {
            e.printStackTrace();
        }
        return;
    }

    @FunctionalInterface
    private interface ControllerSupplier {
        Controller create() throws InvalidCompanyRepIDException;
    }
}