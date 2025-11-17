package cr;
import common.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import exceptions.*;

public class CRHomeRegistry implements ControllerFactory {

    public Map<String, ControllerSupplier> keyMap = new HashMap<>();

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