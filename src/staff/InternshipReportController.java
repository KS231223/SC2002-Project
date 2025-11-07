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

    @SuppressWarnings("LeakingThisInConstructor")
    public InternshipReportController(Router router, Scanner scanner, String staffId, StaffReviewFilters filters) {
        super(router, scanner);
        this.staffId = staffId;
        this.filters = filters;
        this.display = new InternshipReportDisplay(this);
        router.push(this);
    }

    @Override
    public void initialize() {
        List<InternshipEntity> internships = loadInternships();
        StaffReviewFilters.ApplicationStats stats = loadApplicationStats();
        ReportSummary summary = buildSummary(internships, stats);
        display.showReport(summary);
        display.waitForEnter();
        router.pop();
    }

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

    private Map<String, Long> seedStatusCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("Pending", 0L);
        counts.put("Approved", 0L);
        counts.put("Rejected", 0L);
        counts.put("Filled", 0L);
        return counts;
    }

    private Map<String, Long> seedPlacementCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("Filled", 0L);
        counts.put("Unfilled", 0L);
        return counts;
    }

    private void increment(Map<String, Long> counts, String key) {
        counts.merge(key, 1L, Long::sum);
    }

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

    private String displayValue(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

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

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private static final class DateStats {
        private LocalDate earliestOpen;
        private LocalDate latestOpen;
        private LocalDate earliestClose;
        private LocalDate latestClose;

        void recordOpen(LocalDate date) {
            if (earliestOpen == null || date.isBefore(earliestOpen)) {
                earliestOpen = date;
            }
            if (latestOpen == null || date.isAfter(latestOpen)) {
                latestOpen = date;
            }
        }

        void recordClose(LocalDate date) {
            if (earliestClose == null || date.isBefore(earliestClose)) {
                earliestClose = date;
            }
            if (latestClose == null || date.isAfter(latestClose)) {
                latestClose = date;
            }
        }

        LocalDate earliestOpen() {
            return earliestOpen;
        }

        LocalDate latestOpen() {
            return latestOpen;
        }

        LocalDate earliestClose() {
            return earliestClose;
        }

        LocalDate latestClose() {
            return latestClose;
        }
    }

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

    private class InternshipReportDisplay extends common.Display {
        InternshipReportDisplay(Controller owner) {
            super(owner);
        }

        @Override
        public void print_menu() {
            // Not used because the report is generated in one pass.
        }

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

        void waitForEnter() {
            System.out.println();
            System.out.print("Press Enter to return to the staff menu...");
            scanner.nextLine();
        }

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

        private String formatRange(LocalDate start, LocalDate end) {
            String startText = start == null ? "-" : start.toString();
            String endText = end == null ? "-" : end.toString();
            return startText + " to " + endText;
        }
    }
}
