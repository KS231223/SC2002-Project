package common;

import exceptions.*;
import java.util.Scanner;


/**
 * Shared base for company representative flows that preloads CR profile data.
 */
public abstract class CRController extends UserController {

    protected final String companyRepID;
    protected String name;
    protected String companyName;
    protected String department;
    protected String position;
    protected String email;

    /**
     * Loads company representative metadata for the active session.
     *
     * @param router navigation coordinator
     * @param scanner shared console reader
     * @param companyRepID identifier of the logged-in company representative
     * @throws InvalidCompanyRepIDException when the identifier cannot be found or the data file is missing
     */
    public CRController(Router router,Scanner scanner, String companyRepID) throws InvalidCompanyRepIDException {
        super(router, scanner,companyRepID);
        this.companyRepID = companyRepID;

        Entity entity = DatabaseManager.getEntryById(PathResolver.resource("cr.csv"), companyRepID, "CR");
        if (entity == null) {
            throw new InvalidCompanyRepIDException("Invalid company representative ID: " + companyRepID);
        }

        CREntity crEntity = (CREntity) entity;
        this.name = requiredValue(crEntity.get(CREntity.CRField.Name), companyRepID);
        this.companyName = requiredValue(crEntity.get(CREntity.CRField.CompanyName), "");
        this.department = requiredValue(crEntity.get(CREntity.CRField.Department), "");
        this.position = requiredValue(crEntity.get(CREntity.CRField.Position), "");
        this.email = requiredValue(crEntity.get(CREntity.CRField.Email), "");
    }

    @Override
    public abstract void initialize();

    /**
     * Prints the preloaded company representative details for debugging or diagnostics.
     */
    public void printCRInfo() {
        System.out.printf("ID: %s\nName: %s\nCompany: %s\nDepartment: %s\nPosition: %s\nEmail: %s\n",
                companyRepID, name, companyName, department, position, email);
    }

    private String requiredValue(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return fallback;
        }
        return trimmed;
    }
}

