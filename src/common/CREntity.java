package common;

public class CREntity extends Entity {
    public enum CRField { CRID, originalPassword, Name, CompanyName, Department, Position, Email, PreferredMajors, PreferredInternshipLevel, PreferredClosingDate }
    public CREntity(String csvLine) {
        values = csvLine.split(",", -1);
    }
    public CREntity(String id, String originalPassword, String name, String companyName, String department, String position, String email) {
        values = new String[]{id, originalPassword, name, companyName, department, position, email, "None", "None", "None"};
    }
    public String get(CRField f) {
        return values[f.ordinal()];
    }
    public void set(CRField f, String v) {
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
