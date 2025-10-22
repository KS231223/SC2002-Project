package ims;

public class CompanyRepresentative extends User {
    private final String companyName;
    private final String department;
    private final String position;
    private final String email;
    private boolean approved;

    public CompanyRepresentative(
            String id,
            String name,
            String password,
            String companyName,
            String department,
            String position,
            String email,
            boolean approved) {
        super(id, name, password, Role.COMPANY_REPRESENTATIVE);
        this.companyName = companyName;
        this.department = department;
        this.position = position;
        this.email = email;
        this.approved = approved;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getDepartment() {
        return department;
    }

    public String getPosition() {
        return position;
    }

    public String getEmail() {
        return email;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }
}
