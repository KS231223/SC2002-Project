package common;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import exceptions.*;


public abstract class CRController extends UserController {

    protected String companyRepID;
    protected String name;
    protected String companyName;
    protected String department;
    protected String position;
    protected String email;

    // Constructor that loads company rep details based on RepID
    public CRController(Router router,Scanner scanner, String companyRepID) throws InvalidCompanyRepIDException {
        super(router, scanner,companyRepID);
        this.companyRepID = companyRepID;

        // Load company rep details from CSV
    File file = new File(PathResolver.resource("cr.csv"));
        boolean found = false;

        if (!file.exists()) {
            throw new InvalidCompanyRepIDException("Company representative database not found.");
        }

        try (Scanner reader = new Scanner(file)) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(","); // CompanyRepID,Name,CompanyName,Department,Position,Email
                if (parts.length < 6) continue;

                if (parts[0].equals(companyRepID)) {
                    this.name = parts[1];
                    this.companyName = parts[2];
                    this.department = parts[3];
                    this.position = parts[4];
                    this.email = parts[5];
                    found = true;
                    break;
                }
            }
        } catch (IOException e) {
            throw new InvalidCompanyRepIDException("Error reading company_reps.csv: " + e.getMessage());
        }

        if (!found) {
            throw new InvalidCompanyRepIDException("Invalid company representative ID: " + companyRepID);
        }
    }

    // Abstract initialize method from Controller
    @Override
    public abstract void initialize();

    // Optional: helper method to display company rep info
    public void printCRInfo() {
        System.out.printf("ID: %s\nName: %s\nCompany: %s\nDepartment: %s\nPosition: %s\nEmail: %s\n",
                companyRepID, name, companyName, department, position, email);
    }
}

