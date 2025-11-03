package common;

public class InternshipEntity extends Entity {

    public enum InternshipField {
        InternshipID, Title, Description, Level, PreferredMajor,
        OpenDate, CloseDate, Status, CompanyName, CRInCharge, Slots, Visibility
    }
    public InternshipEntity(String csvLine) {
        values = csvLine.split(",", -1);
    }
    public InternshipEntity(String id, String title, String description, String level,
                            String preferredMajor, String openDate, String closeDate,
                            String status, String companyName, String crInCharge,
                            String slots, String visibility) {
        values = new String[]{id, title, description, level, preferredMajor, openDate,
                closeDate, status, companyName, crInCharge, slots, visibility};
    }
    public String get(InternshipField f) {
        return values[f.ordinal()];
    }
    public void set(InternshipField f, String v) {
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
