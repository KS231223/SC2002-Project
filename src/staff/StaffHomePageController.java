package staff;

import common.*;
import exceptions.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

/**
 * Controller that drives the interactive staff home page, allowing users to
 * review pending items and adjust the shared filter state used across review
 * flows.
 */
public class StaffHomePageController extends StaffController {

    private final Display staffDisplay;
    private final StaffReviewFilters filters = new StaffReviewFilters();

    /**
     * Creates a controller for the staff home page.
     *
     * @param router  navigation helper used to swap controllers
     * @param scanner shared input stream for console interactions
     * @param staffID identifier of the logged-in staff member
     * @throws InvalidStaffIDException if {@code staffID} cannot be resolved
     */
    public StaffHomePageController(Router router, Scanner scanner, EntityStore entityStore, String staffID ) throws InvalidStaffIDException {
        super(router, scanner, entityStore, staffID);
        this.staffDisplay = new StaffHomeDisplay(this);
    }

    /**
     * Replaces the current controller on the router stack with this instance.
     */
    public void open() {
        router.replace(this);
    }

    /**
     * Displays a welcome message and starts the staff dashboard menu loop.
     */
    @Override
    public void initialize() {
        System.out.println("Staff Home Page loaded successfully for " + name + "!");
        this.handleMenu();
    }

    /**
     * Handles the primary dashboard menu until the user logs out.
     */
    private void handleMenu(){
        while (true) {
            staffDisplay.print_menu();
            String choice = staffDisplay.get_user_input();

            Map<String, ControllerCreator> factory = buildFactory();
            ControllerCreator creator = factory.get(choice);
            if (creator != null) {
                try {
                    creator.create();
                } catch (InvalidStaffIDException ex) {
                    System.out.println("An error occurred: " + ex.getMessage());
                } catch (RuntimeException ex) {
                    System.out.println("An unexpected error occurred: " + ex.getMessage());
                } catch (Exception ex) {
                    System.out.println("An error occurred: " + ex.getMessage());
                }
                continue;
            }

            switch (choice) {
                case "4" -> editFilters();
                case "5" -> clearFilters();
                case "8" -> { System.out.println("Logging out..."); router.pop(); return; }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private Map<String, ControllerCreator> buildFactory() {
        Map<String, ControllerCreator> m = new HashMap<>();
        m.put("1", () -> Objects.requireNonNull(new ReviewRegistrationController(router, scanner, entityStore, staffID, filters)));
        m.put("2", () -> Objects.requireNonNull(new ReviewInternshipController(router, scanner, entityStore, staffID, filters)));
        m.put("3", () -> Objects.requireNonNull(new ReviewWithdrawalController(router, scanner, entityStore, staffID, filters)));
        m.put("6", () -> Objects.requireNonNull(new InternshipReportController(router, scanner, entityStore, staffID, filters)));
        m.put("7", () -> Objects.requireNonNull(new PasswordChanger(router, scanner, entityStore, userID)));
        return m;
    }

    /**
     * Allows the staff user to configure review filters used by downstream
     * controllers.
     */
    private void editFilters() {
        boolean editing = true;
        while (editing) {
            printFilterSummary();
            System.out.println("=== Staff Review Filter Menu ===");
            System.out.println("1. Filter by status");
            System.out.println("2. Filter by preferred major");
            System.out.println("3. Filter by internship level");
            System.out.println("4. Filter by company name");
            System.out.println("5. Filter by placement status");
            System.out.println("6. Set minimum application count");
            System.out.println("7. Configure date range filters");
            System.out.println("0. Back to staff menu");
            System.out.print("Select an option: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> setStatusFilter();
                case "2" -> setMajorFilter();
                case "3" -> setLevelFilter();
                case "4" -> setCompanyFilter();
                case "5" -> setPlacementStatusFilter();
                case "6" -> setMinimumApplications();
                case "7" -> setDateRangeFilters();
                case "0" -> editing = false;
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    /**
     * Resets all review filters back to their default unconstrained state.
     */
    private void clearFilters() {
        filters.reset();
        System.out.println("All staff review filters cleared.");
    }

    /**
     * Prints a textual summary of the currently active filters to standard
     * output.
     */
    void printFilterSummary() {
        System.out.println("Current filters:");
        System.out.println(" - Status: " + describeCollection(filters.statuses()));
        System.out.println(" - Preferred major: " + describeCollection(filters.majors()));
        System.out.println(" - Level: " + describeCollection(filters.levels()));
        System.out.println(" - Company: " + describeCollection(filters.companies()));
        System.out.println(" - Placement status: " + filters.placementStatus().label());
        System.out.println(" - Minimum applications: " + (filters.minApplications() == null ? "Any" : filters.minApplications()));
        System.out.println(" - Opening date range: " + describeDateRange(filters.openDateRange()));
        System.out.println(" - Closing date range: " + describeDateRange(filters.closeDateRange()));
    }

    /**
     * Prompts the user to adjust the status filter.
     */
    private void setStatusFilter() {
        System.out.println();
        System.out.println("Enter statuses to include (Pending, Approved, Rejected, Filled).");
        System.out.println("Separate multiple values with commas or leave blank to clear the filter.");
        String input = scanner.nextLine();
        updateSetFilter(filters.statuses(), input);
        System.out.println(filters.statuses().isEmpty() ? "Status filter cleared." : "Status filter updated.");
    }

    /**
     * Prompts the user to adjust the preferred major filter.
     */
    private void setMajorFilter() {
        System.out.println();
        System.out.println("Enter preferred majors to include (e.g., CSC, EEE, MAE).");
        System.out.println("Separate multiple values with commas or leave blank to clear the filter.");
        String input = scanner.nextLine();
        updateSetFilter(filters.majors(), input);
        System.out.println(filters.majors().isEmpty() ? "Preferred major filter cleared." : "Preferred major filter updated.");
    }

    /**
     * Prompts the user to adjust the internship level filter.
     */
    private void setLevelFilter() {
        System.out.println();
        System.out.println("Enter internship levels to include (Basic, Intermediate, Advanced).");
        System.out.println("Separate multiple values with commas or leave blank to clear the filter.");
        String input = scanner.nextLine();
        updateSetFilter(filters.levels(), input);
        System.out.println(filters.levels().isEmpty() ? "Internship level filter cleared." : "Internship level filter updated.");
    }

    /**
     * Prompts the user to adjust the company name filter.
     */
    private void setCompanyFilter() {
        System.out.println();
        System.out.println("Enter company names to include. Separate multiple values with commas or leave blank to clear the filter.");
        String input = scanner.nextLine();
        updateSetFilter(filters.companies(), input);
        System.out.println(filters.companies().isEmpty() ? "Company filter cleared." : "Company filter updated.");
    }

    /**
     * Prompts the user to adjust the placement status filter.
     */
    private void setPlacementStatusFilter() {
        System.out.println();
        System.out.println("Select placement status filter:");
        System.out.println("1. Filled");
        System.out.println("2. Unfilled");
        System.out.println("3. Any");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1" -> filters.placementStatus(StaffReviewFilters.PlacementStatus.FILLED);
            case "2" -> filters.placementStatus(StaffReviewFilters.PlacementStatus.UNFILLED);
            case "3" -> filters.placementStatus(StaffReviewFilters.PlacementStatus.ANY);
            default -> System.out.println("Invalid choice. Keeping previous setting.");
        }
    }

    /**
     * Prompts the user to set the minimum number of applications filter.
     */
    private void setMinimumApplications() {
        System.out.println();
        System.out.println("Enter minimum application count (leave blank to clear):");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            filters.minApplications(null);
            System.out.println("Minimum application filter cleared.");
            return;
        }
        try {
            int value = Integer.parseInt(input);
            if (value < 0) {
                System.out.println("Please enter a non-negative number.");
                return;
            }
            filters.minApplications(value);
            System.out.println("Minimum application filter set to " + value + ".");
        } catch (NumberFormatException ex) {
            System.out.println("Invalid number. Please try again.");
        }
    }

    /**
     * Prompts the user to configure opening or closing date range filters.
     */
    private void setDateRangeFilters() {
        System.out.println();
        System.out.println("Date range filters:");
        System.out.println("1. Set opening date range");
        System.out.println("2. Set closing date range");
        System.out.println("3. Clear opening date range");
        System.out.println("4. Clear closing date range");
        System.out.println("0. Back to filter menu");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1" -> filters.openDateRange(requestDateRange("opening"));
            case "2" -> filters.closeDateRange(requestDateRange("closing"));
            case "3" -> {
                filters.openDateRange(null);
                System.out.println("Opening date range cleared.");
            }
            case "4" -> {
                filters.closeDateRange(null);
                System.out.println("Closing date range cleared.");
            }
            case "0" -> { /* back */ }
            default -> System.out.println("Invalid option.");
        }
    }

    /**
     * Requests a date range for the specified label, ensuring that the end date
     * is not earlier than the start date.
     *
     * @param label label describing the range, e.g. {@code "opening"}
     * @return a populated date range or {@code null} if the range should be cleared
     */
    private StaffReviewFilters.DateRange requestDateRange(String label) {
        while (true) {
            LocalDate start = promptDate("Start " + label + " date (yyyy-MM-dd, blank for none): ");
            LocalDate end = promptDate("End " + label + " date (yyyy-MM-dd, blank for none): ");
            if (start != null && end != null && end.isBefore(start)) {
                System.out.println("End date cannot be before start date. Please try again.");
                continue;
            }
            if (start == null && end == null) {
                System.out.println("No values provided; the filter will be cleared.");
                return null;
            }
            return new StaffReviewFilters.DateRange(start, end);
        }
    }

    /**
     * Prompts the user for a date value and validates the format.
     *
     * @param prompt label to display before reading input
     * @return parsed {@link LocalDate} or {@code null} if left blank
     */
    private LocalDate promptDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException ex) {
                System.out.println("Invalid date format. Use yyyy-MM-dd.");
            }
        }
    }

    /**
     * Normalizes user input and stores the tokens in the target filter set.
     *
     * @param target set to populate
     * @param input  raw user input, potentially {@code null}
     */
    private void updateSetFilter(Set<String> target, String input) {
        target.clear();
        if (input == null) {
            return;
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        String[] parts = trimmed.split(",");
        for (String part : parts) {
            String normalized = normalize(part);
            if (!normalized.isEmpty()) {
                target.add(normalized);
            }
        }
    }

    /**
     * Formats the provided collection summary for display.
     *
     * @param values collection to describe
     * @return human-readable description
     */
    private String describeCollection(Set<String> values) {
        if (values.isEmpty()) {
            return "Any";
        }
        return String.join(", ", values);
    }

    /**
     * Formats the provided date range summary for display.
     *
     * @param range range to describe
     * @return human-readable date range
     */
    private String describeDateRange(StaffReviewFilters.DateRange range) {
        if (range == null) {
            return "Any";
        }
        String start = range.start() == null ? "-" : range.start().toString();
        String end = range.end() == null ? "-" : range.end().toString();
        return start + " to " + end;
    }

    /**
     * Normalizes values captured from user input.
     *
     * @param value raw value
     * @return uppercase trimmed value or an empty string when {@code null}
     */
    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

}

// Staff home display moved to `staff.StaffHomeDisplay`