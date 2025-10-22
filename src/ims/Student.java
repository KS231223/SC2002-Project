package ims;

public class Student extends User {
    private final String major;
    private final int yearOfStudy;
    private final String email;

    public Student(String id, String name, String password, String major, int yearOfStudy, String email) {
        super(id, name, password, Role.STUDENT);
        this.major = major;
        this.yearOfStudy = yearOfStudy;
        this.email = email;
    }

    public String getMajor() {
        return major;
    }

    public int getYearOfStudy() {
        return yearOfStudy;
    }

    public String getEmail() {
        return email;
    }
}
