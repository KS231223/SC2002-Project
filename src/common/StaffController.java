package common;

import exceptions.*;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Shared base for staff flows that preloads staff profile data from CSV.
 */
public abstract class StaffController extends UserController {

    protected final String staffID;
    protected String name;
    protected String staffRole;
    protected String department;
    protected String email;

    /**
     * Loads staff metadata based on the provided identifier.
     */
    public StaffController(Router router,Scanner scanner,String staffID) throws InvalidStaffIDException {
        super(router,scanner,staffID);
        this.staffID = staffID;

    File file = new File(PathResolver.resource("staff.csv"));
        boolean found = false;

        if (!file.exists()) {
            throw new InvalidStaffIDException("Staff database not found.");
        }

        try (Scanner reader = new Scanner(file)) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(","); // StaffID,Name,Role,Department,Email
                if (parts.length < 5) continue;

                if (parts[0].equals(staffID)) {
                    this.name = parts[1].trim();
                    this.staffRole = parts[2].trim();
                    this.department = parts[3].trim();
                    this.email = parts[4].trim();
                    found = true;
                    break;
                }
            }
        } catch (IOException e) {
            throw new InvalidStaffIDException("Error reading staff.csv: " + e.getMessage());
        }

        if (!found) {
            throw new InvalidStaffIDException("Invalid staff ID: " + staffID);
        }
    }

    @Override
    public abstract void initialize();

    /**
     * Prints the preloaded staff information for debugging or diagnostics.
     */
    public void printStaffInfo() {
    System.out.printf("ID: %s\nName: %s\nRole: %s\nDepartment: %s\nEmail: %s\n",
        staffID, name, staffRole, department, email);
    }
}
