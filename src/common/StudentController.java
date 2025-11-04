package common;

import exceptions.*;

public abstract class StudentController extends UserController {

    protected String studentID;
    protected String name;
    protected String major;
    protected int year;
    protected String email;
    protected String acceptedInternshipID;

    // Constructor that loads student details based on UID
    public StudentController(Router router, java.util.Scanner scanner, String studentID) throws InvalidStudentIDException {
        super(router, scanner, studentID);
        this.studentID = studentID;

        StudentEntity student = StudentFilterService.loadStudent(studentID);
        if (student == null) {
            throw new InvalidStudentIDException("Invalid student ID: " + studentID);
        }

        this.name = student.get(StudentEntity.StudentField.Name);
        this.major = student.get(StudentEntity.StudentField.Major);
        try {
            this.year = Integer.parseInt(student.get(StudentEntity.StudentField.Year));
        } catch (NumberFormatException ex) {
            throw new InvalidStudentIDException("Invalid year data for student: " + studentID);
        }
        this.email = student.get(StudentEntity.StudentField.Email);
        this.acceptedInternshipID = student.get(StudentEntity.StudentField.AcceptedInternshipID);
    }

    // Abstract initialize method from Controller
    @Override
    public abstract void initialize();

    // Optional: helper method to display student info
    public void printStudentInfo() {
        System.out.printf("ID: %s\nName: %s\nMajor: %s\nYear: %d\nEmail: %s\nAccepted Internship: %s\n",
                studentID, name, major, year, email, acceptedInternshipID);
    }
}
