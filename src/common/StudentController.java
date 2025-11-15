package common;

import exceptions.*;

/**
 * Base controller for student-facing flows that preloads student profile data.
 */
public abstract class StudentController extends UserController {

    protected final String studentID;
    protected String name;
    protected String major;
    protected int year;
    protected String email;
    protected String acceptedInternshipID;

    /**
     * Loads student metadata and validates the identifier.
     */
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

    @Override
    public abstract void initialize();

    /**
     * Prints the preloaded student information for diagnostics.
     */
    public void printStudentInfo() {
        System.out.printf("ID: %s\nName: %s\nMajor: %s\nYear: %d\nEmail: %s\nAccepted Internship: %s\n",
                studentID, name, major, year, email, acceptedInternshipID);
    }
}
