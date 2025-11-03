package common;

public class UserEntity extends Entity {

    public enum UserField { Username, Password, Role }

    public UserEntity(String csvLine) {
        values = csvLine.split(",", -1);
    }

    public UserEntity(String username, String password, String role) {
        values = new String[]{username, password, role};
    }

    public String get(UserField f) {
        return values[f.ordinal()];
    }

    public void set(UserField f, String v) {
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
