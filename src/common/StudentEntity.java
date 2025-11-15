package common;

import java.util.Arrays;

/**
 * Represents a student profile alongside persisted internship filter preferences.
 */
public class StudentEntity extends Entity {

	public static final String NO_FILTER_VALUE = "None";

	/**
	 * Column identifiers for student records.
	 */
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

	/**
	 * Builds a student entity from CSV data, ensuring optional fields have defaults.
	 */
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

	/**
	 * Builds a student entity from discrete values with default filters cleared.
	 */
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

	/**
	 * Retrieves the value of the requested field.
	 */
	public String get(StudentField f) {
		return values[f.ordinal()];
	}

	/**
	 * Updates the value of the requested field.
	 */
	public void set(StudentField f, String v) {
		values[f.ordinal()] = v;
	}

	/**
	 * Convenience accessor for the student identifier.
	 */
	public String getStudentID() {
		return get(StudentField.StudentID);
	}

	/**
	 * Serializes the entity to CSV form.
	 */
	public String toCSV() {
		return super.toCSVFormat();
	}

	@Override
	public String toString() {
		return toCSV();
	}
}
