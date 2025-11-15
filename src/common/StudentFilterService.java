package common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Provides utility methods for loading students and applying student-side internship filters.
 */
public final class StudentFilterService {

    private static final String STUDENT_FILE = PathResolver.resource("student.csv");
    private static final String INTERNSHIP_FILE = PathResolver.resource("internship_opportunities.csv");
    public static final String SORT_SOONEST = "Soonest";
    public static final String SORT_LATEST = "Latest";

    private StudentFilterService() {
    }

    /**
     * Loads a student record by identifier.
     */
    public static StudentEntity loadStudent(String studentId) {
        Entity entity = DatabaseManager.getEntryById(STUDENT_FILE, studentId, "Student");
        if (entity == null) {
            return null;
        }
        return (StudentEntity) entity;
    }

    /**
     * Persists the supplied student entity back to storage.
     */
    public static void saveStudent(StudentEntity student) {
        if (student == null) {
            return;
        }
        DatabaseManager.updateEntry(STUDENT_FILE, student.getStudentID(), student, "Student");
    }

    /**
     * Resets the filter fields on the provided student entity without persisting.
     */
    public static void resetFilters(StudentEntity student) {
        if (student == null) {
            return;
        }
        student.set(StudentEntity.StudentField.FilterMajor, StudentEntity.NO_FILTER_VALUE);
        student.set(StudentEntity.StudentField.FilterLevel, StudentEntity.NO_FILTER_VALUE);
        student.set(StudentEntity.StudentField.FilterCompany, StudentEntity.NO_FILTER_VALUE);
        student.set(StudentEntity.StudentField.FilterStatus, StudentEntity.NO_FILTER_VALUE);
        student.set(StudentEntity.StudentField.FilterClosingSort, StudentEntity.NO_FILTER_VALUE);
    }

    /**
     * Clears persisted filters for the student identified by {@code studentId}.
     */
    public static void clearFilters(String studentId) {
        StudentEntity student = loadStudent(studentId);
        if (student == null) {
            throw new IllegalArgumentException("Student not found: " + studentId);
        }
        resetFilters(student);
        saveStudent(student);
    }

    /**
     * Extracts normalized filter values from the supplied student entity.
     */
    public static StudentFilters extractFilters(StudentEntity student) {
        if (student == null) {
            return new StudentFilters(StudentEntity.NO_FILTER_VALUE, StudentEntity.NO_FILTER_VALUE,
                    StudentEntity.NO_FILTER_VALUE, StudentEntity.NO_FILTER_VALUE, StudentEntity.NO_FILTER_VALUE);
        }
    return new StudentFilters(
        normalize(student.get(StudentEntity.StudentField.FilterLevel)),
        normalize(student.get(StudentEntity.StudentField.FilterCompany)),
        normalize(student.get(StudentEntity.StudentField.FilterStatus)),
        normalize(student.get(StudentEntity.StudentField.FilterMajor)),
        normalize(student.get(StudentEntity.StudentField.FilterClosingSort))
    );
    }

    /**
     * Determines whether an internship passes the supplied filters.
     */
    public static boolean matchesFilters(InternshipEntity internship, StudentFilters filters) {
        if (internship == null || filters == null) {
            return false;
        }
        String visibility = internship.get(InternshipEntity.InternshipField.Visibility);
        if (visibility == null || !"VISIBLE".equalsIgnoreCase(visibility.trim())) {
            return false;
        }
        if (filters.hasLevel() && !equalsIgnoreCase(internship.get(InternshipEntity.InternshipField.Level), filters.level())) {
            return false;
        }
        if (filters.hasCompany() && !equalsIgnoreCase(internship.get(InternshipEntity.InternshipField.CompanyName), filters.company())) {
            return false;
        }
        if (filters.hasStatus() && !equalsIgnoreCase(internship.get(InternshipEntity.InternshipField.Status), filters.status())) {
            return false;
        }
        if (!filters.hasMajor()) {
            return true;
        }
        return equalsIgnoreCase(
            internship.get(InternshipEntity.InternshipField.PreferredMajor),
            filters.major()
        );
    }

    /**
     * Sorts internships using the requested closing-date preference.
     */
    public static void sortInternships(List<InternshipEntity> internships, StudentFilters filters) {
        if (internships == null || filters == null) {
            return;
        }
    Comparator<InternshipEntity> alphabetical = Comparator.comparing(
        internship -> valueOrEmpty(internship.get(InternshipEntity.InternshipField.Title)),
        String.CASE_INSENSITIVE_ORDER);
    Comparator<InternshipEntity> byCloseDateAsc = Comparator.comparing(
        (InternshipEntity internship) -> parseCloseDate(internship),
        Comparator.nullsLast(Comparator.naturalOrder()));
    Comparator<InternshipEntity> byCloseDateDesc = Comparator.comparing(
        (InternshipEntity internship) -> parseCloseDate(internship),
        Comparator.nullsLast(Comparator.reverseOrder()));
    Comparator<InternshipEntity> comparator;
        if (filters.sortBySoonest()) {
        comparator = byCloseDateAsc.thenComparing(alphabetical);
        } else if (filters.sortByLatest()) {
        comparator = byCloseDateDesc.thenComparing(alphabetical);
        } else {
        comparator = alphabetical;
        }
        internships.sort(comparator);
    }

    /**
     * Lists company names extracted from internships, in alphabetical order.
     */
    public static List<String> listCompanies() {
        List<Entity> internships = DatabaseManager.getDatabase(INTERNSHIP_FILE, new ArrayList<>(), "Internship");
        Set<String> companies = new LinkedHashSet<>();
        for (Entity entity : internships) {
            InternshipEntity internship = (InternshipEntity) entity;
            String company = normalize(internship.get(InternshipEntity.InternshipField.CompanyName));
            if (hasValue(company) && !companies.contains(company)) {
                companies.add(company);
            }
        }
        List<String> companyList = new ArrayList<>(companies);
        companyList.sort(String.CASE_INSENSITIVE_ORDER);
        return companyList;
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return StudentEntity.NO_FILTER_VALUE;
        }
        String cleaned = value.trim();
        return cleaned.equalsIgnoreCase(StudentEntity.NO_FILTER_VALUE) ? StudentEntity.NO_FILTER_VALUE : cleaned;
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private static boolean equalsIgnoreCase(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return left.trim().equalsIgnoreCase(right.trim());
    }

    private static boolean hasValue(String value) {
        return value != null && !value.equalsIgnoreCase(StudentEntity.NO_FILTER_VALUE);
    }

    private static LocalDate parseCloseDate(InternshipEntity internship) {
        return parseCloseDate(internship.get(InternshipEntity.InternshipField.CloseDate));
    }

    private static LocalDate parseCloseDate(String closeDate) {
        if (closeDate == null || closeDate.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(closeDate.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH));
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    /**
     * Immutable bundle of normalized student filter selections.
     */
    public record StudentFilters(String level,
                                 String company,
                                 String status,
                                 String major,
                                 String closingSort) {

        public boolean hasLevel() {
            return hasValue(level);
        }

        public boolean hasCompany() {
            return hasValue(company);
        }

        public boolean hasStatus() {
            return hasValue(status);
        }

        public boolean hasMajor() {
            return hasValue(major);
        }

        public boolean sortBySoonest() {
            return equalsIgnoreCase(closingSort, SORT_SOONEST);
        }

        public boolean sortByLatest() {
            return equalsIgnoreCase(closingSort, SORT_LATEST);
        }

        public String closingSortDisplay() {
            if (sortBySoonest()) {
                return "Closing Date: Soonest first";
            }
            if (sortByLatest()) {
                return "Closing Date: Latest first";
            }
            return "Alphabetical by title";
        }

        public boolean hasActiveFilters() {
            return hasLevel() || hasCompany() || hasStatus() || hasMajor() || sortBySoonest() || sortByLatest();
        }

        public String level() {
            return level;
        }

        public String company() {
            return company;
        }

        public String status() {
            return status;
        }

        public String major() {
            return major;
        }

        public String closingSort() {
            return closingSort;
        }
    }
}
