package common;

/**
 * Represents a company representative's persisted profile details.
 */
public class CREntity extends Entity {
    /**
     * Column identifiers for CR records.
     */
    public enum CRField { CRID, originalPassword, Name, CompanyName, Department, Position, Email, PreferredMajors, PreferredInternshipLevel, PreferredClosingDate }

    /**
     * Creates an entity from a line in the CR CSV file.
     *
     * @param csvLine persisted values
     */
    public CREntity(String csvLine) {
        values = csvLine.split(",", -1);
    }

    /**
     * Creates a CR entity using discrete values, defaulting filter fields to {@code "None"}.
     */
    public CREntity(String id, String originalPassword, String name, String companyName, String department, String position, String email) {
        values = new String[]{id, originalPassword, name, companyName, department, position, email, "None", "None", "None"};
    }

    /**
     * Returns the stored value for the requested CR field.
     */
    public String get(CRField f) {
        return values[f.ordinal()];
    }

    /**
     * Stores a new value for the requested CR field.
     */
    public void set(CRField f, String v) {
        values[f.ordinal()] = v;
    }

    /**
     * Serializes the entity into CSV form.
     */
    public String toCSV() {
        return super.toCSVFormat();
    }

    @Override
    public String toString() {
        return toCSV();
    }
}
