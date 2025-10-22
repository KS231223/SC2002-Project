package ims;

public class CareerCenterStaff extends User {
    private final String department;
    private final String email;

    public CareerCenterStaff(String id, String name, String password, String department, String email) {
        super(id, name, password, Role.CAREER_STAFF);
        this.department = department;
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public String getEmail() {
        return email;
    }
}
