package common;

public class ApplicationEntity extends Entity {

    public enum ApplicationField {
        ApplicationID, StudentID, InternshipID, Status, SubmissionDate
    }
    public ApplicationEntity(String csvLine) {
        values = csvLine.split(",", -1);
    }
    public ApplicationEntity(String appID, String studentID, String internshipID,
                             String status, String submissionDate) {
        values = new String[]{appID, studentID, internshipID, status, submissionDate};
    }
    public String get(ApplicationField f) {
        return values[f.ordinal()];
    }
    public void set(ApplicationField f, String v) {
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
