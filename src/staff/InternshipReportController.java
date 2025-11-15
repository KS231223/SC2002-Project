package staff;

import common.ApplicationEntity;
import common.Controller;
import common.DatabaseManager;
import common.Entity;
import common.InternshipEntity;
import common.PathResolver;
import common.Router;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

/**
 * Generates a summary report of internships using the shared staff filters.
 */
public class InternshipReportController extends Controller {

    private static final String INTERNSHIP_FILE = PathResolver.resource("internship_opportunities.csv");
    private static final String APPLICATION_FILE = PathResolver.resource("internship_applications.csv");

    private final InternshipReportDisplay display;
    private final StaffReviewFilters filters;
    private final String staffId;

    /**
     * Builds a controller that generates a report backed by the shared filters.
     *
     * @param router  router managing navigation stack
     * @param scanner shared input reader
     * @param staffId identifier of the staff member requesting the report
     * @param filters shared filters limiting the data included in the report
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public InternshipReportController(Router router, Scanner scanner, String staffId, StaffReviewFilters filters) {
        super(router, scanner);
        this.staffId = staffId;
        this.filters = filters;
        this.display = new InternshipReportDisplay(this);
        router.push(this);
    }

    /**
     * Loads data, generates the report summary, and displays it to the user.
     */
    @Override
    public void initialize() {
        List<InternshipEntity> internships = loadInternships();
        StaffReviewFilters.ApplicationStats stats = loadApplicationStats();
        ReportSummary summary = buildSummary(internships, stats);
        display.showReport(summary);
        display.waitForEnter();
        router.pop();
    }

    /**
     * Retrieves all internship records from persistent storage.
     *
     * @return list of internships, potentially empty
     */
    private List<InternshipEntity> loadInternships() {
        List<InternshipEntity> internships = new ArrayList<>();
        List<Entity> rawInternships = DatabaseManager.getDatabase(INTERNSHIP_FILE, new ArrayList<>(), "Internship");
        for (Entity entity : rawInternships) {
            if (entity instanceof InternshipEntity internship) {
                internships.add(internship);
            }
        }
        return internships;
    }

    /**
     * Builds aggregate application statistics for use in the report.
     *
     * @return application statistics keyed by internship ID
     */
    private StaffReviewFilters.ApplicationStats loadApplicationStats() {
        Map<String, Long> totalCounts = new HashMap<>();
        Map<String, Long> acceptedCounts = new HashMap<>();
        List<Entity> rawApplications = DatabaseManager.getDatabase(APPLICATION_FILE, new ArrayList<>(), "Application");
        for (Entity entity : rawApplications) {
            if (entity instanceof ApplicationEntity application) {
                String internshipId = application.get(ApplicationEntity.ApplicationField.InternshipID);
                if (internshipId == null) {
                    continue;
                }
                totalCounts.merge(internshipId, 1L, Long::sum);
                if ("ACCEPTED".equalsIgnoreCase(application.get(ApplicationEntity.ApplicationField.Status))) {
                    acceptedCounts.merge(internshipId, 1L, Long::sum);
                }
            }
        }
        return new StaffReviewFilters.ApplicationStats(totalCounts, acceptedCounts);
    }

    /**
     * Constructs a report summary applying the current filters to the provided data.
     *
     * @param internships internships to evaluate
     * @param stats       application statistics supporting the report
     * @return populated report summary
     */
    private ReportSummary buildSummary(List<InternshipEntity> internships, StaffReviewFilters.ApplicationStats stats) {
        Map<String, Long> statusCounts = seedStatusCounts();
        Map<String, Long> placementCounts = seedPlacementCounts();
        Map<String, Long> majorCounts = new TreeMap<>();
        Map<String, Long> levelCounts = new TreeMap<>();
        Map<String, Long> companyCounts = new TreeMap<>();
        DateStats dateStats = new DateStats();

        int matched = 0;
        long totalApplications = 0;
        long acceptedApplications = 0;
        int internshipsWithApplications = 0;

        for (InternshipEntity internship : internships) {
            String internshipId = internship.get(InternshipEntity.InternshipField.InternshipID);
            long total = stats.totalFor(internshipId);
            long accepted = stats.acceptedFor(internshipId);

            if (!filters.matchesInternship(internship, total, accepted)) {
                continue;
            }

            matched++;
            totalApplications += total;
            acceptedApplications += accepted;
            if (total > 0) {
                internshipsWithApplications++;
            }

            increment(statusCounts, formatStatus(internship.get(InternshipEntity.InternshipField.Status)));
            increment(majorCounts, displayValue(internship.get(InternshipEntity.InternshipField.PreferredMajor), "Unknown"));
            increment(levelCounts, displayValue(internship.get(InternshipEntity.InternshipField.Level), "Unknown"));
            increment(companyCounts, displayValue(internship.get(InternshipEntity.InternshipField.CompanyName), "Unknown"));

            boolean filled = isFilled(internship, accepted);
            increment(placementCounts, filled ? "Filled" : "Unfilled");

            updateDateStats(dateStats, internship.get(InternshipEntity.InternshipField.OpenDate), true);
            updateDateStats(dateStats, internship.get(InternshipEntity.InternshipField.CloseDate), false);
        }

        return new ReportSummary(
                staffId,
                filters,
                matched,
                totalApplications,
                acceptedApplications,
                internshipsWithApplications,
                statusCounts,
                majorCounts,
                levelCounts,
                companyCounts,
                placementCounts,
                dateStats
        );
    }

    /**
     * Initialises the status breakdown map with common statuses.
     *
     * @return map seeded with zero counts per status
     */
    private Map<String, Long> seedStatusCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("Pending", 0L);
        counts.put("Approved", 0L);
        counts.put("Rejected", 0L);
        counts.put("Filled", 0L);
        return counts;
    }

    /**
     * Initialises the placement status breakdown map.
     *
     * @return map seeded with zero counts per placement state
     */
    private Map<String, Long> seedPlacementCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("Filled", 0L);
        counts.put("Unfilled", 0L);
        return counts;
    }

    /**
     * Increments the counter associated with the provided key.
     *
     * @param counts target counter map
     * @param key    bucket to increment
     */
    private void increment(Map<String, Long> counts, String key) {
        counts.merge(key, 1L, Long::sum);
    }

    /**
     * Formats raw status strings into human-readable labels.
     *
     * @param raw raw status value
     * @return formatted status label
     */
    private String formatStatus(String raw) {
        String normalized = normalize(raw);
        return switch (normalized) {
            case "PENDING" -> "Pending";
            case "APPROVED" -> "Approved";
            case "REJECTED" -> "Rejected";
            case "FILLED" -> "Filled";
            default -> displayValue(raw, "Unknown");
        };
    }

    /**
     * Returns a display value that falls back to the provided default when empty.
     *
     * @param value    value to display
     * @param fallback fallback text when the value is blank
     * @return formatted value suitable for output
     */
    private String displayValue(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    /**
     * Determines whether an internship should be considered filled based on
     * slot capacity and accepted application counts.
     *
     * @param internship    internship under evaluation
     * @param acceptedCount number of accepted applications
     * @return {@code true} when the internship is filled
     */
    private boolean isFilled(InternshipEntity internship, long acceptedCount) {
        String slotsValue = internship.get(InternshipEntity.InternshipField.Slots);
        try {
            int slots = Integer.parseInt(slotsValue.trim());
            if (slots <= 0) {
                return acceptedCount > 0;
            }
            return acceptedCount >= slots;
        } catch (NumberFormatException ex) {
            return acceptedCount > 0;
        }
    }

    /**
     * Updates aggregate date statistics for opening or closing dates.
     *
     * @param stats       date accumulator
     * @param value       raw date value
     * @param openingDate {@code true} when tracking opening dates
     */
    private void updateDateStats(DateStats stats, String value, boolean openingDate) {
        LocalDate date = parseDate(value);
        if (date == null) {
            return;
        }
        if (openingDate) {
            stats.recordOpen(date);
        } else {
            stats.recordClose(date);
        }
    }

    /**
     * Parses ISO-8601 date strings into {@link LocalDate} instances.
     *
     * @param value raw text to parse
     * @return parsed date or {@code null} when invalid or blank
     */
    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    /**
     * Normalises values for case-insensitive comparisons.
     *
     * @param value raw text
     * @return uppercase trimmed value or empty string when {@code null}
     */
    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Aggregates min/max opening and closing dates for the report output.
     */
    private static final class DateStats {
        private LocalDate earliestOpen;
        private LocalDate latestOpen;
        private LocalDate earliestClose;
        private LocalDate latestClose;

        /**
         * Records an opening date, updating min/max trackers.
         *
         * @param date date to capture
         */
        void recordOpen(LocalDate date) {
            if (earliestOpen == null || date.isBefore(earliestOpen)) {
                earliestOpen = date;
            }
            if (latestOpen == null || date.isAfter(latestOpen)) {
                latestOpen = date;
            }
        }

        /**
         * Records a closing date, updating min/max trackers.
         *
         * @param date date to capture
         */
        void recordClose(LocalDate date) {
            if (earliestClose == null || date.isBefore(earliestClose)) {
                earliestClose = date;
            }
            if (latestClose == null || date.isAfter(latestClose)) {
                latestClose = date;
            }
        }

        /**
         * @return earliest captured opening date or {@code null}
         */
        LocalDate earliestOpen() {
            return earliestOpen;
        }

        /**
         * @return latest captured opening date or {@code null}
         */
        LocalDate latestOpen() {
            return latestOpen;
        }

        /**
         * @return earliest captured closing date or {@code null}
         */
        LocalDate earliestClose() {
            return earliestClose;
        }

        /**
         * @return latest captured closing date or {@code null}
         */
        LocalDate latestClose() {
            return latestClose;
        }
    }

    /**
     * Immutable data transfer object summarising report metrics for display.
     */
    private record ReportSummary(
            String staffId,
            StaffReviewFilters filters,
            int matchedInternships,
            long totalApplications,
            long acceptedApplications,
            int internshipsWithApplications,
            Map<String, Long> statusCounts,
            Map<String, Long> majorCounts,
            Map<String, Long> levelCounts,
            Map<String, Long> companyCounts,
            Map<String, Long> placementCounts,
            DateStats dateStats
    ) { }

    /**
     * Display helper responsible for presenting the generated internship report.
     */
    private class InternshipReportDisplay extends common.Display {
        /**
         * Creates a display facade bound to the owning controller.
         *
         * @param owner controller coordinating the display
         */
        InternshipReportDisplay(Controller owner) {
            super(owner);
        }

        /**
         * Not used because the report prints immediately without a menu.
         */
        @Override
        public void print_menu() {
            // Not used because the report is generated in one pass.
        }

        /**
         * Renders the report breakdown to the console.
         *
         * @param summary aggregated report data
         */
        void showReport(ReportSummary summary) {
            System.out.println();
            System.out.println("=== Internship Report ===");
            System.out.println("Staff ID: " + summary.staffId());
            printFilterSummary(summary.filters());
            System.out.println();
            System.out.println("Internships matching filters: " + summary.matchedInternships());
            System.out.println("Total applications: " + summary.totalApplications()
                    + " (accepted: " + summary.acceptedApplications() + ")");
            System.out.println("Internships with applications > 0: " + summary.internshipsWithApplications());
            System.out.println();
            printCounts("Internship status", summary.statusCounts());
            printCounts("Preferred major", summary.majorCounts());
            printCounts("Internship level", summary.levelCounts());
            printCounts("Company name", summary.companyCounts());
            printCounts("Placement status", summary.placementCounts());
            printDateStats(summary.dateStats());
        }

        /**
         * Waits for the user to acknowledge the report before returning.
         */
        void waitForEnter() {
            System.out.println();
            System.out.print("Press Enter to return to the staff menu...");
            scanner.nextLine();
        }

        /**
         * Prints a summary of the filters that produced the report.
         *
         * @param filters filters applied to the dataset
         */
        private void printFilterSummary(StaffReviewFilters filters) {
            System.out.println();
            System.out.println("Applied filters:");
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
         * Formats a collection for display, returning "Any" when empty.
         *
         * @param values values to describe
         * @return human-readable description
         */
        private String describeCollection(Set<String> values) {
            if (values.isEmpty()) {
                return "Any";
            }
            return String.join(", ", values);
        }

        /**
         * Formats a date range for display, returning "Any" when not constrained.
         *
         * @param range range to describe
         * @return textual representation of the range
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
         * Prints the contents of a counter map using the provided label.
         *
         * @param label  heading for the section
         * @param counts metrics to display
         */
        private void printCounts(String label, Map<String, Long> counts) {
            System.out.println(label + " breakdown:");
            if (counts.isEmpty()) {
                System.out.println("  None");
                System.out.println();
                return;
            }
            for (Map.Entry<String, Long> entry : counts.entrySet()) {
                System.out.println("  " + entry.getKey() + ": " + entry.getValue());
            }
            System.out.println();
        }

        /**
         * Prints the aggregated date ranges captured in the summary.
         *
         * @param stats date statistics to describe
         */
        private void printDateStats(DateStats stats) {
            System.out.println("Date range overview:");
            if (stats.earliestOpen() == null && stats.earliestClose() == null) {
                System.out.println("  No matching internships with date information.");
                System.out.println();
                return;
            }
            if (stats.earliestOpen() != null || stats.latestOpen() != null) {
                System.out.println("  Opening dates: " + formatRange(stats.earliestOpen(), stats.latestOpen()));
            }
            if (stats.earliestClose() != null || stats.latestClose() != null) {
                System.out.println("  Closing dates: " + formatRange(stats.earliestClose(), stats.latestClose()));
            }
            System.out.println();
        }

        /**
         * Formats the supplied range endpoints into a textual interval.
         *
         * @param start start date
         * @param end   end date
         * @return human-readable range string
         */
        private String formatRange(LocalDate start, LocalDate end) {
            String startText = start == null ? "-" : start.toString();
            String endText = end == null ? "-" : end.toString();
            return startText + " to " + endText;
        }
    }
}
