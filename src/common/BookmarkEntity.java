package common;

/**
 * Entity representing a student's bookmark of an internship opportunity.
 */
public class BookmarkEntity extends Entity {

    /**
     * Fields exposed by the bookmark CSV layout.
     */
    public enum BookmarkField {
        StudentID, InternshipID
    }

    /**
     * Creates a bookmark entity by splitting an existing CSV row.
     *
     * @param csvLine raw CSV row containing student and internship IDs
     */
    public BookmarkEntity(String csvLine) {
        values = csvLine.split(",", -1);
    }

    /**
     * Creates a bookmark entity directly from identifiers.
     *
     * @param studentID    unique student identifier
     * @param internshipID unique internship identifier
     */
    public BookmarkEntity(String studentID, String internshipID) {
        values = new String[]{studentID, internshipID};
    }

    /**
     * Retrieves a value by bookmark field.
     *
     * @param f bookmark field to read
     * @return value stored for the field
     */
    public String get(BookmarkField f) {
        return values[f.ordinal()];
    }

    /**
     * Updates the value of a bookmark field.
     *
     * @param f bookmark field to update
     * @param v new value to store
     */
    public void set(BookmarkField f, String v) {
        values[f.ordinal()] = v;
    }

    /**
     * Converts the entity to a CSV-formatted string.
     *
     * @return CSV row representing the bookmark
     */
    public String toCSV() {
        return super.toCSVFormat();
    }

    @Override
    public String toString() {
        return toCSV();
    }
}
