package common;


/**
 * Coordinates state transitions for internship applications written to CSV storage.
 */
public class ApplicationHandler {
    private static final String APPLICATION_FILE = PathResolver.resource("internship_applications.csv");
    private static final String INTERNSHIP_FILE = PathResolver.resource("internship_opportunities.csv");
    private static final String STUDENT_FILE = PathResolver.resource("student.csv");
    private final EntityStore entityStore;

    public ApplicationHandler(EntityStore entityStore) {
        this.entityStore = entityStore;
    }
    /*Internship => String id, String title, String description, String level,
                            String preferredMajor, String openDate, String closeDate,
                            String status, String companyName, String crInCharge,
                            String slots, String visibility*
      Application => String appID, String studentID, String internshipID,
                             String status, String submissionDate
     */
    /**
     * Marks the specified application as withdrawn and frees any reserved slot.
     *
     * @param applicationId identifier of the application to withdraw
     */
    public void withdrawApplication(String applicationId){
        try {
            Entity applicationToWithdraw = entityStore.findById(APPLICATION_FILE, applicationId, "Application");
            String internshipId = applicationToWithdraw.getArrayValueByIndex(2);
            Entity internshipToChange = entityStore.findById(INTERNSHIP_FILE, internshipId, "Internship");
            String internshipSlots = internshipToChange.getArrayValueByIndex(10);

            String applicationStatus = applicationToWithdraw.getArrayValueByIndex(3);
            switch(applicationStatus) {
                case "Accepted":
                case "Approved":
                    internshipToChange.setArrayValueByIndex(10,modifySlots(internshipSlots, 1));
                    entityStore.update(INTERNSHIP_FILE, internshipId, internshipToChange, "Internship");
                    internshipToChange.setArrayValueByIndex(7, "Pending");
                default:
                    applicationToWithdraw.setArrayValueByIndex(3,"WITHDRAWN");
                    entityStore.update(APPLICATION_FILE, applicationId, applicationToWithdraw, "Application");



            }

        } catch (Exception e) {
            System.err.println("ERROR IN WITHDRAWING APPLICATION");
        }
    }

    /**
     * Approves the specified application and decrements the available internship slots.
     *
     * @param applicationId identifier of the application to approve
     */
    public void approveApplication(String applicationId){
        try {

            Entity applicationToApprove = entityStore.findById(APPLICATION_FILE, applicationId, "Application");
            String internshipId = applicationToApprove.getArrayValueByIndex(2);
            Entity internshipToChange = entityStore.findById(INTERNSHIP_FILE, internshipId, "Internship");
            String internshipSlots = internshipToChange.getArrayValueByIndex(10);

            String applicationStatus = applicationToApprove.getArrayValueByIndex(3);

            if (!applicationStatus.equals("WITHDRAWN") && !applicationStatus.equals("Accepted") && !internshipSlots.equals("0")) {
                applicationToApprove.setArrayValueByIndex(3, "Approved");
                internshipToChange.setArrayValueByIndex(10, modifySlots(internshipSlots, -1));
                if(modifySlots(internshipSlots, -1).equals("0")){
                    internshipToChange.setArrayValueByIndex(7, "FILLED");
                }

                entityStore.update(INTERNSHIP_FILE, internshipId, internshipToChange, "Internship");
                entityStore.update(APPLICATION_FILE, applicationId, applicationToApprove, "Application");
            }

        } catch (Exception e) {
            System.err.println("ERROR IN APPROVING APPLICATION");
        }
    }

    /**
     * Records a student's acceptance of an approved application and stores the company on the student profile.
     *
     * @param applicationId identifier of the application to accept
     */
    public void acceptApplication(String applicationId){
        try {
            Entity applicationToAccept = entityStore.findById(APPLICATION_FILE, applicationId, "Application");
            Entity internshipToAccept = entityStore.findById(INTERNSHIP_FILE,applicationToAccept.getArrayValueByIndex(2),"Internship");
            String applicationCompany = internshipToAccept.getArrayValueByIndex(8);

            String applicationStatus = applicationToAccept.getArrayValueByIndex(3);
            if (applicationStatus.equals("Approved")) {
                applicationToAccept.setArrayValueByIndex(3, "Accepted");
                entityStore.update(APPLICATION_FILE, applicationId, applicationToAccept, "Application");
                String studentId = applicationToAccept.getArrayValueByIndex(1);
                Entity thisStudent = entityStore.findById(STUDENT_FILE,studentId,"Student");
                thisStudent.setArrayValueByIndex(9,applicationCompany);
                entityStore.update(STUDENT_FILE,studentId,thisStudent,"Student");

            }

        } catch (Exception e) {
            System.err.println("ERROR IN ACCEPTING APPLICATION");
        }
    }
    /**
     * Computes the updated slot count for an internship.
     *
     * @param value original slot count
     * @param numberToAdd delta to apply
     * @return new slot count as a string
     */
    private static String modifySlots(String value, int numberToAdd){
        int valueToChange = Integer.parseInt(value);
        valueToChange += numberToAdd;
        return Integer.toString(valueToChange);
    }

}
