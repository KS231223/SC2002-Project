package common;

import java.util.Arrays;

public class StudentEntity extends Entity {

    public static final String NO_FILTER_VALUE = "None";

    public enum StudentField {
        StudentID,
        Name,
        Major,
        Year,
        Email,
        FilterMajor,
        FilterLevel,
        FilterCompany,
        FilterStatus,
        FilterClosingSort,
        AcceptedInternshipID
    }

    public StudentEntity(String csvLine) {
        String[] parsed = csvLine.split(",", -1);
        int expectedLength = StudentField.values().length;
        values = new String[expectedLength];
        Arrays.fill(values, NO_FILTER_VALUE);

        for (int i = 0; i < expectedLength; i++) {
            if (i < parsed.length) {
                String cleaned = parsed[i].trim();
                values[i] = cleaned.isEmpty() ? defaultValueForIndex(i) : cleaned;
            } else {
                values[i] = defaultValueForIndex(i);
            }
        }
    }

    public StudentEntity(String id, String name, String major, String year, String email) {
        values = new String[StudentField.values().length];
        values[StudentField.StudentID.ordinal()] = id;
        values[StudentField.Name.ordinal()] = name;
        values[StudentField.Major.ordinal()] = major;
        values[StudentField.Year.ordinal()] = year;
        values[StudentField.Email.ordinal()] = email;
        values[StudentField.FilterMajor.ordinal()] = NO_FILTER_VALUE;
        values[StudentField.FilterLevel.ordinal()] = NO_FILTER_VALUE;
        values[StudentField.FilterCompany.ordinal()] = NO_FILTER_VALUE;
        values[StudentField.FilterStatus.ordinal()] = NO_FILTER_VALUE;
        values[StudentField.FilterClosingSort.ordinal()] = NO_FILTER_VALUE;
        values[StudentField.AcceptedInternshipID.ordinal()] = NO_FILTER_VALUE;
    }

    private String defaultValueForIndex(int index) {
        if (index <= StudentField.Email.ordinal()) {
            return "";
        }
        return NO_FILTER_VALUE;
    }

    public String get(StudentField f) {
        return values[f.ordinal()];
    }

    public void set(StudentField f, String v) {
        values[f.ordinal()] = v;
    }

    public String getStudentID() {
        return get(StudentField.StudentID);
    }

    public String toCSV() {
        return super.toCSVFormat();
    }

    @Override
    public String toString() {
        return toCSV();
    }
}
