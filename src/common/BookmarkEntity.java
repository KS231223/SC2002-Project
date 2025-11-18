package common;

public class BookmarkEntity extends Entity {

    public enum BookmarkField {
        StudentID, InternshipID
    }

    public BookmarkEntity(String csvLine) {
        values = csvLine.split(",", -1);
    }

    public BookmarkEntity(String studentID, String internshipID) {
        values = new String[]{studentID, internshipID};
    }

    public String get(BookmarkField f) {
        return values[f.ordinal()];
    }

    public void set(BookmarkField f, String v) {
        values[f.ordinal()] = v;
    }

    public String toCSV() {
        return super.toCSVFormat();
    }

    @Override
    public String toString() {
        return toCSV();
    }
}
