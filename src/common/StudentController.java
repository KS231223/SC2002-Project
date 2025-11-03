package common;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import exceptions.*;

public abstract class StudentController extends UserController {

    protected String studentID;
    protected String name;
    protected String major;
    protected int year;
    protected String email;
    protected String acceptedInternshipID;

    // Constructor that loads student details based on UID
    public StudentController(Router router,Scanner scanner, String studentID) throws InvalidStudentIDException {
        super(router,scanner,studentID);
        this.studentID = studentID;

        // Load student details from CSV
        File file = new File("resources/student.csv");
        boolean found = false;

        if (!file.exists()) {
            throw new InvalidStudentIDException("Student database not found.");
        }

        try (Scanner reader = new Scanner(file)) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(","); // StudentID,Name,Major,Year,Email,Accepted InternshipID
                if (parts.length < 5) continue;

                if (parts[0].equals(studentID)) {
                    this.name = parts[1];
                    this.major = parts[2];
                    this.year = Integer.parseInt(parts[3]);
                    this.email = parts[4];
                    this.acceptedInternshipID = (parts.length > 5) ? parts[5] : "";
                    found = true;
                    break;
                }
            }
        } catch (IOException e) {
            throw new InvalidStudentIDException("Error reading students.csv: " + e.getMessage());
        }

        if (!found) {
            throw new InvalidStudentIDException("Invalid student ID: " + studentID);
        }
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
