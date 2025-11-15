package common;

/**
 * Represents a single internship application persisted in the CSV backing store.
 */
public class ApplicationEntity extends Entity {

    /**
     * Identifiers for the persisted fields of an application record.
     */
    public enum ApplicationField {
        ApplicationID, StudentID, InternshipID, Status, SubmissionDate
    }

    /**
     * Creates an entity from a raw CSV entry.
     *
     * @param csvLine persisted application values
     */
    public ApplicationEntity(String csvLine) {
        values = csvLine.split(",", -1);
    }

    /**
     * Creates an application entity from discrete values.
     *
     * @param appID unique identifier of the application
     * @param studentID identifier of the applicant
     * @param internshipID identifier of the internship applied for
     * @param status current status value
     * @param submissionDate ISO-8601 submission date
     */
    public ApplicationEntity(String appID, String studentID, String internshipID,
                             String status, String submissionDate) {
        values = new String[]{appID, studentID, internshipID, status, submissionDate};
    }

    /**
     * Returns the stored value for the requested field.
     *
     * @param f field identifier
     * @return stored value, possibly empty
     */
    public String get(ApplicationField f) {
        return values[f.ordinal()];
    }

    /**
     * Updates the stored value for the requested field.
     *
     * @param f field identifier
     * @param v new value to store
     */
    public void set(ApplicationField f, String v) {
        values[f.ordinal()] = v;
    }

    /**
     * Serializes the entity back to CSV format.
     *
     * @return CSV representation of the entity
     */
    public String toCSV() {
        return super.toCSVFormat();
    }

    @Override
    public String toString() {
        return toCSV();
    }
}
