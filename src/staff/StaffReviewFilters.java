package staff;

import common.ApplicationEntity;
import common.CREntity;
import common.InternshipEntity;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Shared filter state applied across staff review flows.
 */
public final class StaffReviewFilters {

    private final Set<String> statuses = new LinkedHashSet<>();
    private final Set<String> majors = new LinkedHashSet<>();
    private final Set<String> levels = new LinkedHashSet<>();
    private final Set<String> companies = new LinkedHashSet<>();
    private PlacementStatus placementStatus = PlacementStatus.ANY;
    private Integer minApplications;
    private DateRange openDateRange;
    private DateRange closeDateRange;

    /**
     * Returns the mutable set of status filters.
     *
     * @return collection containing allowed statuses
     */
    public Set<String> statuses() {
        return statuses;
    }

    /**
     * Returns the mutable set of preferred major filters.
     *
     * @return collection containing allowed majors
     */
    public Set<String> majors() {
        return majors;
    }

    /**
     * Returns the mutable set of internship level filters.
     *
     * @return collection containing allowed levels
     */
    public Set<String> levels() {
        return levels;
    }

    /**
     * Returns the mutable set of company name filters.
     *
     * @return collection containing allowed companies
     */
    public Set<String> companies() {
        return companies;
    }

    /**
     * Retrieves the placement status constraint.
     *
     * @return selected placement constraint
     */
    public PlacementStatus placementStatus() {
        return placementStatus;
    }

    /**
     * Updates the placement status constraint.
     *
     * @param placementStatus new placement filter value
     */
    public void placementStatus(PlacementStatus placementStatus) {
        this.placementStatus = placementStatus;
    }

    /**
     * Returns the minimum application count constraint.
     *
     * @return minimum applications required or {@code null}
     */
    public Integer minApplications() {
        return minApplications;
    }

    /**
     * Updates the minimum application count constraint.
     *
     * @param minApplications threshold to apply, {@code null} to clear
     */
    public void minApplications(Integer minApplications) {
        this.minApplications = minApplications;
    }

    /**
     * Returns the opening date range constraint.
     *
     * @return date range or {@code null}
     */
    public DateRange openDateRange() {
        return openDateRange;
    }

    /**
     * Updates the opening date range constraint.
     *
     * @param openDateRange range to apply, {@code null} to clear
     */
    public void openDateRange(DateRange openDateRange) {
        this.openDateRange = openDateRange;
    }

    /**
     * Returns the closing date range constraint.
     *
     * @return date range or {@code null}
     */
    public DateRange closeDateRange() {
        return closeDateRange;
    }

    /**
     * Updates the closing date range constraint.
     *
     * @param closeDateRange range to apply, {@code null} to clear
     */
    public void closeDateRange(DateRange closeDateRange) {
        this.closeDateRange = closeDateRange;
    }

    /**
     * Resets all filters to their default unrestricted values.
     */
    public void reset() {
        statuses.clear();
        majors.clear();
        levels.clear();
        companies.clear();
        placementStatus = PlacementStatus.ANY;
        minApplications = null;
        openDateRange = null;
        closeDateRange = null;
    }

    /**
     * Determines whether a registration request satisfies the current filters.
     *
     * @param entity company representative registration under review
     * @return {@code true} when the entity matches the filters
     */
    @SuppressWarnings("RedundantIfStatement")
    public boolean matchesRegistration(CREntity entity) {
        if (entity == null) {
            return false;
        }
        boolean companyOk = companies.isEmpty() || companies.contains(normalize(entity.get(CREntity.CRField.CompanyName)));
        boolean majorOk = majors.isEmpty() || containsAnyToken(entity.get(CREntity.CRField.PreferredMajors), majors);
        boolean levelOk = levels.isEmpty() || levels.contains(normalize(entity.get(CREntity.CRField.PreferredInternshipLevel)));
        boolean closingDateOk = dateMatches(entity.get(CREntity.CRField.PreferredClosingDate), closeDateRange);
        return companyOk && majorOk && levelOk && closingDateOk;
    }

    /**
     * Determines whether an internship submission satisfies the current filters.
     *
     * @param entity        internship submission under review
     * @param totalApps     total applications received for the internship
     * @param acceptedApps  accepted applications for the internship
     * @return {@code true} when the submission matches the filters
     */
    @SuppressWarnings("RedundantIfStatement")
    public boolean matchesInternship(InternshipEntity entity, long totalApps, long acceptedApps) {
        if (entity == null) {
            return false;
        }
        boolean statusOk = statuses.isEmpty() || statuses.contains(normalize(entity.get(InternshipEntity.InternshipField.Status)));
    boolean majorOk = majors.isEmpty() || containsAnyToken(entity.get(InternshipEntity.InternshipField.PreferredMajor), majors);
        boolean levelOk = levels.isEmpty() || levels.contains(normalize(entity.get(InternshipEntity.InternshipField.Level)));
        boolean companyOk = companies.isEmpty() || companies.contains(normalize(entity.get(InternshipEntity.InternshipField.CompanyName)));
        boolean openDateOk = dateMatches(entity.get(InternshipEntity.InternshipField.OpenDate), openDateRange);
        boolean closeDateOk = dateMatches(entity.get(InternshipEntity.InternshipField.CloseDate), closeDateRange);
        boolean placementOk = placementStatus.matches(isFilled(entity, acceptedApps));
        boolean minOk = meetsMinimumApplications(totalApps);
        return statusOk && majorOk && levelOk && companyOk && openDateOk && closeDateOk && placementOk && minOk;
    }

    /**
     * Determines whether a withdrawal request satisfies the current filters.
     *
     * @param application   withdrawal request under review
     * @param internship    corresponding internship, may be {@code null}
     * @param totalApps     total applications submitted for the internship
     * @param acceptedApps  accepted applications for the internship
     * @return {@code true} when the request matches the filters
     */
    @SuppressWarnings("RedundantIfStatement")
    public boolean matchesWithdrawal(ApplicationEntity application,
                                     InternshipEntity internship,
                                     long totalApps,
                                     long acceptedApps) {
        if (application == null) {
            return false;
        }
        if (!statuses.isEmpty() && !statuses.contains(normalize(application.get(ApplicationEntity.ApplicationField.Status)))) {
            return false;
        }
        if (internship != null) {
            if (!majors.isEmpty() && !containsAnyToken(internship.get(InternshipEntity.InternshipField.PreferredMajor), majors)) {
                return false;
            }
            if (!levels.isEmpty() && !levels.contains(normalize(internship.get(InternshipEntity.InternshipField.Level)))) {
                return false;
            }
            if (!companies.isEmpty() && !companies.contains(normalize(internship.get(InternshipEntity.InternshipField.CompanyName)))) {
                return false;
            }
            if (!dateMatches(internship.get(InternshipEntity.InternshipField.OpenDate), openDateRange)) {
                return false;
            }
            if (!dateMatches(internship.get(InternshipEntity.InternshipField.CloseDate), closeDateRange)) {
                return false;
            }
            if (!placementStatus.matches(isFilled(internship, acceptedApps))) {
                return false;
            }
        } else {
            if (!majors.isEmpty() || !levels.isEmpty() || !companies.isEmpty()
                    || openDateRange != null || closeDateRange != null || placementStatus == PlacementStatus.FILLED || placementStatus == PlacementStatus.UNFILLED) {
                return false;
            }
        }
    return meetsMinimumApplications(totalApps);
    }

    private boolean containsAnyToken(String source, Set<String> filter) {
        if (source == null) {
            return false;
        }
        String normalizedWhole = normalize(source);
        if (!normalizedWhole.isEmpty() && filter.contains(normalizedWhole)) {
            return true;
        }
        String[] tokens = source.split("[;,|/\\\\]+");
        for (String token : tokens) {
            String normalized = normalize(token);
            if (!normalized.isEmpty() && filter.contains(normalized)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("RedundantIfStatement")
    private boolean dateMatches(String value, DateRange range) {
        if (range == null || (range.start == null && range.end == null)) {
            return true;
        }
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            LocalDate date = LocalDate.parse(value.trim());
            boolean afterStart = range.start == null || !date.isBefore(range.start);
            boolean beforeEnd = range.end == null || !date.isAfter(range.end);
            return afterStart && beforeEnd;
        } catch (DateTimeParseException ex) {
            return false;
        }
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

    private boolean meetsMinimumApplications(long totalApps) {
        if (minApplications == null) {
            return true;
        }
        return totalApps >= minApplications;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Immutable range representing optional start and end dates.
     *
     * @param start earliest acceptable date, {@code null} for open-ended
     * @param end   latest acceptable date, {@code null} for open-ended
     */
    public record DateRange(LocalDate start, LocalDate end) { }

    /**
     * Enumerates placement filter options available to staff reviewers.
     */
    public enum PlacementStatus {
        ANY("Any"),
        FILLED("Filled"),
        UNFILLED("Unfilled");

        private final String label;

        PlacementStatus(String label) {
            this.label = label;
        }

        /**
         * Evaluates whether the provided flag satisfies the placement constraint.
         *
         * @param filled whether the internship is considered filled
         * @return {@code true} if the constraint permits the provided value
         */
        public boolean matches(boolean filled) {
            return this == ANY || (this == FILLED && filled) || (this == UNFILLED && !filled);
        }

        /**
         * Human-readable label for display.
         *
         * @return formatted label
         */
        public String label() {
            return label;
        }
    }

    /**
     * Aggregates application counts for internships, supporting filter checks.
     */
    public static final class ApplicationStats {
        private final Map<String, Long> totalCounts;
        private final Map<String, Long> acceptedCounts;

        /**
         * Creates a statistics bundle for internship applications.
         *
         * @param totalCounts    application count by internship identifier
         * @param acceptedCounts accepted application count by internship identifier
         */
        public ApplicationStats(Map<String, Long> totalCounts, Map<String, Long> acceptedCounts) {
            this.totalCounts = totalCounts;
            this.acceptedCounts = acceptedCounts;
        }

        /**
         * Retrieves the total application count for a specific internship.
         *
         * @param internshipId identifier of the internship
         * @return total applications recorded for the internship
         */
        public long totalFor(String internshipId) {
            return totalCounts.getOrDefault(internshipId, 0L);
        }

        /**
         * Retrieves the accepted application count for a specific internship.
         *
         * @param internshipId identifier of the internship
         * @return accepted applications recorded for the internship
         */
        public long acceptedFor(String internshipId) {
            return acceptedCounts.getOrDefault(internshipId, 0L);
        }
    }
}
