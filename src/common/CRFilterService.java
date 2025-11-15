package common;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides in-memory filtering utilities for Company Representatives.
 */
public final class CRFilterService {

    public static final String NO_FILTER_VALUE = "ALL";

    private static final Map<String, String> STATUS_CANONICAL = Map.of(
        "pending", "Pending",
        "approved", "Approved",
        "rejected", "Rejected",
        "filled", "Filled"
    );
    private static final Map<String, String> VISIBILITY_CANONICAL = Map.of(
        "on", "Visible",
        "visible", "Visible",
        "off", "Hidden",
        "hidden", "Hidden"
    );
    private static final Map<String, String> LEVEL_CANONICAL = Map.of(
        "basic", "Basic",
        "intermediate", "Intermediate",
        "advanced", "Advanced"
    );

    private static final ConcurrentHashMap<String, CRFilters> FILTERS = new ConcurrentHashMap<>();

    private CRFilterService() {
    }

    /**
     * Retrieves the active filters for the given company representative.
     *
     * @param crId company representative identifier
     * @return stored filters or an empty filter set when none are stored
     */
    public static CRFilters getFilters(String crId) {
        if (crId == null) {
            return CRFilters.empty();
        }
        return FILTERS.getOrDefault(crId, CRFilters.empty());
    }

    /**
     * Persists normalized filter values for a company representative.
     *
     * @param crId company representative identifier
     * @param status desired status filter
     * @param visibility desired visibility filter
     * @param level desired level filter
     * @param major desired major filter
     */
    public static void updateFilters(String crId, String status, String visibility, String level, String major) {
        if (crId == null) {
            return;
        }
        CRFilters filters = new CRFilters(status, visibility, level, major);
        if (filters.hasActiveFilters()) {
            FILTERS.put(crId, filters);
        } else {
            FILTERS.remove(crId);
        }
    }

    /**
     * Removes any stored filters for the given company representative.
     *
     * @param crId company representative identifier
     */
    public static void clearFilters(String crId) {
        if (crId == null) {
            return;
        }
        FILTERS.remove(crId);
    }

    /**
     * Normalizes a free-form status input into a canonical value.
     */
    public static Optional<String> normalizeStatus(String input) {
        return normalizeWithCanonical(input, STATUS_CANONICAL);
    }

    /**
     * Normalizes visibility input to either {@code Visible} or {@code Hidden}.
     */
    public static Optional<String> normalizeVisibility(String input) {
        return normalizeWithCanonical(input, VISIBILITY_CANONICAL);
    }

    /**
     * Normalizes internship level input.
     */
    public static Optional<String> normalizeLevel(String input) {
        return normalizeWithCanonical(input, LEVEL_CANONICAL);
    }

    /**
     * Normalizes major strings, returning {@link #NO_FILTER_VALUE} when empty.
     */
    public static String normalizeMajor(String input) {
        String cleaned = cleaned(input);
        if (cleaned.isEmpty() || cleaned.equalsIgnoreCase(NO_FILTER_VALUE)) {
            return NO_FILTER_VALUE;
        }
        return cleaned;
    }

    /**
     * Checks whether a given internship satisfies the supplied filters.
     */
    public static boolean matchesInternship(InternshipEntity internship, CRFilters filters) {
        if (internship == null) {
            return false;
        }
        if (filters == null || !filters.hasActiveFilters()) {
            return true;
        }

        if (filters.hasStatus() && !equalsIgnoreCase(internship.get(InternshipEntity.InternshipField.Status), filters.status())) {
            return false;
        }

        if (filters.hasVisibility()) {
            String actualVisibility = canonicalVisibility(internship.get(InternshipEntity.InternshipField.Visibility));
            if (!equalsIgnoreCase(actualVisibility, filters.visibility())) {
                return false;
            }
        }

        if (filters.hasLevel() && !equalsIgnoreCase(internship.get(InternshipEntity.InternshipField.Level), filters.level())) {
            return false;
        }

        if (!filters.hasMajor()) {
            return true;
        }
        return matchesMajor(
            internship.get(InternshipEntity.InternshipField.PreferredMajor),
            filters.major()
        );
    }

    /**
     * Determines whether the internship is owned by the supplied company.
     */
    public static boolean belongsToCompany(InternshipEntity internship, String companyName) {
        if (internship == null || companyName == null) {
            return false;
        }
        return equalsIgnoreCase(internship.get(InternshipEntity.InternshipField.CompanyName), companyName);
    }

    /**
     * Generates a human-readable summary of the stored filters.
     */
    public static String summarize(String crId) {
        return getFilters(crId).summary();
    }

    /**
     * Lists allowed status filter values.
     */
    public static List<String> allowedStatuses() {
        return new ArrayList<>(STATUS_CANONICAL.values());
    }

    /**
     * Lists allowed visibility filter tokens.
     */
    public static List<String> allowedVisibilityStates() {
        return List.of("ON", "OFF");
    }

    /**
     * Lists allowed internship level filter values.
     */
    public static List<String> allowedLevels() {
        return new ArrayList<>(LEVEL_CANONICAL.values());
    }

    private static Optional<String> normalizeWithCanonical(String input, Map<String, String> canonicalMap) {
        String cleaned = cleaned(input);
        if (cleaned.isEmpty() || cleaned.equalsIgnoreCase(NO_FILTER_VALUE)) {
            return Optional.of(NO_FILTER_VALUE);
        }
        String canonical = canonicalMap.get(cleaned.toLowerCase(Locale.ENGLISH));
        if (canonical != null) {
            return Optional.of(canonical);
        }
        return Optional.empty();
    }

    private static String canonicalVisibility(String value) {
        if (value == null) {
            return NO_FILTER_VALUE;
        }
        String cleaned = cleaned(value);
        if (cleaned.isEmpty()) {
            return NO_FILTER_VALUE;
        }
        String canonical = VISIBILITY_CANONICAL.get(cleaned.toLowerCase(Locale.ENGLISH));
        if (canonical != null) {
            return canonical;
        }
        // fall back to original value when not recognized
        return value;
    }

    private static boolean equalsIgnoreCase(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return left.trim().equalsIgnoreCase(right.trim());
    }

    private static boolean matchesMajor(String source, String filter) {
        if (source == null || filter == null) {
            return false;
        }
        String normalizedFilter = filter.trim().toUpperCase(Locale.ENGLISH);
        if (normalizedFilter.isEmpty()) {
            return false;
        }

        String normalizedSource = source.trim().toUpperCase(Locale.ENGLISH);
        if (normalizedSource.equals(normalizedFilter)) {
            return true;
        }

        String[] tokens = normalizedSource.split("[;,|/]+");
        for (String token : tokens) {
            String trimmed = token.trim();
            if (!trimmed.isEmpty() && trimmed.equals(normalizedFilter)) {
                return true;
            }
        }
        return false;
    }

    private static String cleaned(String input) {
        return input == null ? "" : input.trim();
    }

    /**
     * Immutable bundle describing the active filters for a company representative.
     */
    public record CRFilters(String status, String visibility, String level, String major) {

        public CRFilters(String status, String visibility, String level, String major) {
            this.status = valueOrDefault(status);
            this.visibility = valueOrDefault(visibility);
            this.level = valueOrDefault(level);
            this.major = valueOrDefault(major);
        }

        public static CRFilters empty() {
            return new CRFilters(NO_FILTER_VALUE, NO_FILTER_VALUE, NO_FILTER_VALUE, NO_FILTER_VALUE);
        }

        public boolean hasStatus() {
            return !equalsIgnoreCase(status, NO_FILTER_VALUE);
        }

        public boolean hasVisibility() {
            return !equalsIgnoreCase(visibility, NO_FILTER_VALUE);
        }

        public boolean hasLevel() {
            return !equalsIgnoreCase(level, NO_FILTER_VALUE);
        }

        public boolean hasMajor() {
            return !equalsIgnoreCase(major, NO_FILTER_VALUE);
        }

        public boolean hasActiveFilters() {
            return hasStatus() || hasVisibility() || hasLevel() || hasMajor();
        }

        public String summary() {
            if (!hasActiveFilters()) {
                return "None";
            }
            Map<String, String> descriptors = new LinkedHashMap<>();
            if (hasStatus()) {
                descriptors.put("Status", status);
            }
            if (hasVisibility()) {
                descriptors.put("Visibility", visibility);
            }
            if (hasLevel()) {
                descriptors.put("Level", level);
            }
            if (hasMajor()) {
                descriptors.put("Major", major);
            }
            List<String> parts = new ArrayList<>();
            for (Map.Entry<String, String> entry : descriptors.entrySet()) {
                parts.add(entry.getKey() + "=" + entry.getValue());
            }
            return parts.isEmpty() ? "None" : String.join(", ", parts);
        }

        public String status() {
            return status;
        }

        public String visibility() {
            return visibility;
        }

        public String level() {
            return level;
        }

        public String major() {
            return major;
        }

        private static String valueOrDefault(String value) {
            if (value == null) {
                return NO_FILTER_VALUE;
            }
            String cleaned = value.trim();
            return cleaned.isEmpty() ? NO_FILTER_VALUE : cleaned;
        }
    }
}
