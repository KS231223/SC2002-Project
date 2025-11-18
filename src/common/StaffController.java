package common;

import exceptions.*;
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
    public StaffController(Router router, Scanner scanner, EntityStore entityStore, String staffID) throws InvalidStaffIDException {
        super(router, scanner, entityStore, staffID);
        this.staffID = staffID;

        Entity entity = entityStore.findById(PathResolver.resource("staff.csv"), staffID, "Staff");
        if (!(entity instanceof StaffEntity staff)) {
            throw new InvalidStaffIDException("Invalid staff ID: " + staffID);
        }

        this.name = staff.get(StaffEntity.StaffField.Name);
        this.staffRole = staff.get(StaffEntity.StaffField.Role);
        this.department = staff.get(StaffEntity.StaffField.Department);
        this.email = staff.get(StaffEntity.StaffField.Email);
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
