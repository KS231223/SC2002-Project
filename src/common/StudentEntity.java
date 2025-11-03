package common;

public class StudentEntity extends Entity {
    public enum StudentField { StudentID, Name, Major, Year, Email, PreferredMajors, PreferredInternshipLevel, PreferredClosingDate, InternshipStatus, AcceptedInternshipID }
    public StudentEntity(String csvLine) {
        values = csvLine.split(",", -1);
    }
    public StudentEntity(String id, String name, String major, String year, String email) {
        values = new String[]{id, name, major, year, email, "None", "None", "None", "None", "None"};
    }
    public String get(StudentField f) {
        return values[f.ordinal()];
    }
    public void set(StudentField f, String v) {
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
