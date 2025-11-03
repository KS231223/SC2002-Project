package common;
public class StaffEntity extends Entity {
    public enum StaffField { StaffID, Name, Role, Department, Email,  PreferredMajors, PreferredInternshipLevel, PreferredClosingDate, InternshipStatus }
    public StaffEntity(String csvLine) {
        values = csvLine.split(",", -1);
    }
    public StaffEntity(String staffID, String name, String role, String department, String email) {
        values = new String[]{staffID, name, role, department, email, "None", "None", "None", "None"};
    }
    public String get(StaffField f) {
        return values[f.ordinal()];
    }
    public void set(StaffField f, String v) {
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
