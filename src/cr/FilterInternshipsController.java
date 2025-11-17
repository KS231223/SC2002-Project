package cr;

import common.CRController;
import common.CRFilterService;
import common.Controller;
import common.Display;
import common.EntityStore;
import common.Router;
import exceptions.InvalidCompanyRepIDException;
import java.util.Optional;
import java.util.Scanner;

public class FilterInternshipsController extends CRController {

    private final FilterInternshipsDisplay display;

    @SuppressWarnings("LeakingThisInConstructor")
    public FilterInternshipsController(Router router, Scanner scanner, EntityStore entityStore, String crID) throws InvalidCompanyRepIDException {
        super(router, scanner, entityStore, crID);
        this.display = new FilterInternshipsDisplay(this);
        router.push(this);
    }

    @Override
    public void initialize() {
        try {
            System.out.println("=== Internship Filters ===");
            CRFilterService.CRFilters current = CRFilterService.getFilters(userID);
            System.out.println("Current filters: " + current.summary());
            System.out.println("Leave input blank to keep the current value. Enter 'ALL' to clear a filter.");

            String status = promptStatus(current.status());
            String visibility = promptVisibility(current.visibility());
            String level = promptLevel(current.level());
            String major = promptMajor(current.major());

            CRFilterService.updateFilters(userID, status, visibility, level, major);
            CRFilterService.CRFilters updated = CRFilterService.getFilters(userID);
            System.out.println("Updated filters: " + updated.summary());
        } catch (Exception ex) {
            System.err.println("Error updating filters: " + ex.getMessage());
        } finally {
            router.pop();
        }
    }

    private String promptStatus(String currentValue) {
        while (true) {
            String input = display.ask("Status [" + describeValue(currentValue) + "] (" + String.join("/", CRFilterService.allowedStatuses()) + "): ");
            if (input.isBlank()) {
                return currentValue;
            }
            Optional<String> normalized = CRFilterService.normalizeStatus(input);
            if (normalized.isPresent()) {
                return normalized.get();
            }
            System.out.println("Invalid status. Please enter one of: " + String.join(", ", CRFilterService.allowedStatuses()));
        }
    }

    private String promptVisibility(String currentValue) {
        while (true) {
            String input = display.ask("Visibility [" + describeVisibility(currentValue) + "] (ON/OFF): ");
            if (input.isBlank()) {
                return currentValue;
            }
            Optional<String> normalized = CRFilterService.normalizeVisibility(input);
            if (normalized.isPresent()) {
                return normalized.get();
            }
            System.out.println("Invalid visibility. Please enter 'ON' or 'OFF'.");
        }
    }

    private String promptLevel(String currentValue) {
        while (true) {
            String input = display.ask("Internship level [" + describeValue(currentValue) + "] (" + String.join("/", CRFilterService.allowedLevels()) + "): ");
            if (input.isBlank()) {
                return currentValue;
            }
            Optional<String> normalized = CRFilterService.normalizeLevel(input);
            if (normalized.isPresent()) {
                return normalized.get();
            }
            System.out.println("Invalid level. Please enter one of: " + String.join(", ", CRFilterService.allowedLevels()));
        }
    }

    private String promptMajor(String currentValue) {
        String input = display.ask("Preferred major [" + describeValue(currentValue) + "]: ");
        if (input.isBlank()) {
            return currentValue;
        }
        return CRFilterService.normalizeMajor(input);
    }

    private String describeValue(String value) {
        return CRFilterService.NO_FILTER_VALUE.equalsIgnoreCase(value) ? "None" : value;
    }

    private String describeVisibility(String value) {
        if (CRFilterService.NO_FILTER_VALUE.equalsIgnoreCase(value)) {
            return "None";
        }
        return value.equalsIgnoreCase("Visible") ? "ON" : value.equalsIgnoreCase("Hidden") ? "OFF" : value;
    }
}

// FilterInternshipsDisplay moved to `cr.FilterInternshipsDisplay`
