package common;

/**
 * Represents an internship opportunity record.
 */
public class InternshipEntity extends Entity {

    /**
     * Column identifiers for internship records.
     */
    public enum InternshipField {
        InternshipID, Title, Description, Level, PreferredMajor,
        OpenDate, CloseDate, Status, CompanyName, CRInCharge, Slots, Visibility
    }

    /**
     * Builds an entity from CSV data.
     */
    public InternshipEntity(String csvLine) {
        values = csvLine.split(",", -1);
    }

    /**
     * Builds an internship entity from discrete field values.
     */
    public InternshipEntity(String id, String title, String description, String level,
                            String preferredMajor, String openDate, String closeDate,
                            String status, String companyName, String crInCharge,
                            String slots, String visibility) {
        values = new String[]{id, title, description, level, preferredMajor, openDate,
                closeDate, status, companyName, crInCharge, slots, visibility};
    }

    /**
     * Retrieves the value of the requested field.
     */
    public String get(InternshipField f) {
        return values[f.ordinal()];
    }

    /**
     * Updates the value of the requested field.
     */
    public void set(InternshipField f, String v) {
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
