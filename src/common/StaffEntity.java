package common;

/**
 * Represents staff metadata along with optional preference fields.
 */
public class StaffEntity extends Entity {
    /**
     * Column identifiers for staff records.
     */
    public enum StaffField { StaffID, Name, Role, Department, Email,  PreferredMajors, PreferredInternshipLevel, PreferredClosingDate, InternshipStatus }

    /**
     * Creates a staff entity from CSV data.
     */
    public StaffEntity(String csvLine) {
        values = csvLine.split(",", -1);
    }

    /**
     * Creates a staff entity with default preference values set to {@code "None"}.
     */
    public StaffEntity(String staffID, String name, String role, String department, String email) {
        values = new String[]{staffID, name, role, department, email, "None", "None", "None", "None"};
    }

    /**
     * Retrieves the value of the requested field.
     */
    public String get(StaffField f) {
        return values[f.ordinal()];
    }

    /**
     * Updates the value of the requested field.
     */
    public void set(StaffField f, String v) {
        values[f.ordinal()] = v;
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
