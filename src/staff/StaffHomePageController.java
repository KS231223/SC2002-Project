package staff;

import common.*;
import exceptions.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

public class StaffHomePageController extends StaffController {

    private final Display staffDisplay;
    private final StaffReviewFilters filters = new StaffReviewFilters();

    public StaffHomePageController(Router router, Scanner scanner, String staffID ) throws InvalidStaffIDException {
        super(router, scanner, staffID);
        this.staffDisplay = new StaffHomeDisplay(this);
    }

    public void open() {
        router.replace(this);
    }

    @Override
    public void initialize() {
        System.out.println("Staff Home Page loaded successfully for " + name + "!");
        this.handleMenu();
    }

    private void handleMenu(){
        while (true) {
            staffDisplay.print_menu();
            String choice = staffDisplay.get_user_input();

            switch (choice) {
                case "1" -> Objects.requireNonNull(new ReviewRegistrationController(router, scanner, staffID, filters));
                case "2" -> Objects.requireNonNull(new ReviewInternshipController(router, scanner, staffID, filters));
                case "3" -> Objects.requireNonNull(new ReviewWithdrawalController(router, scanner, staffID, filters));
                case "4" -> editFilters();
                case "5" -> clearFilters();
                case "6" -> Objects.requireNonNull(new InternshipReportController(router, scanner, staffID, filters));
                case "7" -> Objects.requireNonNull(new PasswordChanger(router, scanner, userID));
                case "8" -> { System.out.println("Logging out..."); router.pop(); return; }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

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

    private void clearFilters() {
        filters.reset();
        System.out.println("All staff review filters cleared.");
    }

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

    private void setStatusFilter() {
        System.out.println();
        System.out.println("Enter statuses to include (Pending, Approved, Rejected, Filled).");
        System.out.println("Separate multiple values with commas or leave blank to clear the filter.");
        String input = scanner.nextLine();
        updateSetFilter(filters.statuses(), input);
        System.out.println(filters.statuses().isEmpty() ? "Status filter cleared." : "Status filter updated.");
    }

    private void setMajorFilter() {
        System.out.println();
        System.out.println("Enter preferred majors to include (e.g., CSC, EEE, MAE).");
        System.out.println("Separate multiple values with commas or leave blank to clear the filter.");
        String input = scanner.nextLine();
        updateSetFilter(filters.majors(), input);
        System.out.println(filters.majors().isEmpty() ? "Preferred major filter cleared." : "Preferred major filter updated.");
    }

    private void setLevelFilter() {
        System.out.println();
        System.out.println("Enter internship levels to include (Basic, Intermediate, Advanced).");
        System.out.println("Separate multiple values with commas or leave blank to clear the filter.");
        String input = scanner.nextLine();
        updateSetFilter(filters.levels(), input);
        System.out.println(filters.levels().isEmpty() ? "Internship level filter cleared." : "Internship level filter updated.");
    }

    private void setCompanyFilter() {
        System.out.println();
        System.out.println("Enter company names to include. Separate multiple values with commas or leave blank to clear the filter.");
        String input = scanner.nextLine();
        updateSetFilter(filters.companies(), input);
        System.out.println(filters.companies().isEmpty() ? "Company filter cleared." : "Company filter updated.");
    }

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

    private String describeCollection(Set<String> values) {
        if (values.isEmpty()) {
            return "Any";
        }
        return String.join(", ", values);
    }

    private String describeDateRange(StaffReviewFilters.DateRange range) {
        if (range == null) {
            return "Any";
        }
        String start = range.start() == null ? "-" : range.start().toString();
        String end = range.end() == null ? "-" : range.end().toString();
        return start + " to " + end;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

}


    // Private inner display class for staff home page


class StaffHomeDisplay extends Display {

    private final StaffHomePageController staffHome;

    public StaffHomeDisplay(Controller owner) {
        super(owner);
        this.staffHome = (StaffHomePageController) owner;
    }

    @Override
    public void print_menu() {
        System.out.println("Welcome! Displaying staff dashboard...");
        System.out.println("=== Staff Home Page ===");
        System.out.println("=== Career Center Staff Menu ===");
        staffHome.printFilterSummary();
        System.out.println("1. Review company representative registrations");
        System.out.println("2. Review internship submissions");
        System.out.println("3. Handle withdrawal requests");
        System.out.println("4. Update review filters");
        System.out.println("5. Clear review filters");
    System.out.println("6. Generate internship report");
    System.out.println("7. Change password");
    System.out.println("8. Logout");
        System.out.print("Select an option: ");
    }
}