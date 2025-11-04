package common;


public class ApplicationHandler {
    private static final String APPLICATION_FILE = PathResolver.resource("internship_applications.csv");
    private static final String INTERNSHIP_FILE = PathResolver.resource("internship_opportunities.csv");
    private static final String STUDENT_FILE = PathResolver.resource("student.csv");
    /*Internship => String id, String title, String description, String level,
                            String preferredMajor, String openDate, String closeDate,
                            String status, String companyName, String crInCharge,
                            String slots, String visibility*
      Application => String appID, String studentID, String internshipID,
                             String status, String submissionDate
     */

    public static void withdrawApplication(String applicationId){
        try {
            Entity applicationToWithdraw = DatabaseManager.getEntryById(APPLICATION_FILE, applicationId, "Application");
            String internshipId = applicationToWithdraw.getArrayValueByIndex(2);
            Entity internshipToChange = DatabaseManager.getEntryById(INTERNSHIP_FILE, internshipId, "Internship");
            String internshipSlots = internshipToChange.getArrayValueByIndex(10);

            String applicationStatus = applicationToWithdraw.getArrayValueByIndex(3);
            switch(applicationStatus) {
                case "Accepted":
                case "Approved":
                    internshipToChange.setArrayValueByIndex(10,modifySlots(internshipSlots, 1));
                    DatabaseManager.updateEntry(INTERNSHIP_FILE, internshipId,internshipToChange, "Internship");
                default:
                    applicationToWithdraw.setArrayValueByIndex(3,"WITHDRAWN");
                    DatabaseManager.updateEntry(APPLICATION_FILE, applicationId, applicationToWithdraw, "Application");
            }

        } catch (Exception e) {
            System.err.println("ERROR IN WITHDRAWING APPLICATION");
        }

    }
    public static void approveApplication(String applicationId){
        try {

            Entity applicationToApprove = DatabaseManager.getEntryById(APPLICATION_FILE, applicationId, "Application");
            String internshipId = applicationToApprove.getArrayValueByIndex(2);
            Entity internshipToChange = DatabaseManager.getEntryById(INTERNSHIP_FILE, internshipId, "Internship");
            String internshipSlots = internshipToChange.getArrayValueByIndex(10);

            String applicationStatus = applicationToApprove.getArrayValueByIndex(3);

            if (!applicationStatus.equals("WITHDRAWN") && !applicationStatus.equals("Accepted") && !internshipSlots.equals("0")) {
                applicationToApprove.setArrayValueByIndex(3, "Approved");
                internshipToChange.setArrayValueByIndex(10, modifySlots(internshipSlots, -1));
                if(modifySlots(internshipSlots, -1).equals("0")){
                    internshipToChange.setArrayValueByIndex(7, "FILLED");
                }

                DatabaseManager.updateEntry(INTERNSHIP_FILE, internshipId, internshipToChange, "Internship");
                DatabaseManager.updateEntry(APPLICATION_FILE, applicationId, applicationToApprove, "Application");
            }

        } catch (Exception e) {
            System.err.println("ERROR IN APPROVING APPLICATION");
        }
    }
    public static void acceptApplication(String applicationId){
        try {
            Entity applicationToAccept = DatabaseManager.getEntryById(APPLICATION_FILE, applicationId, "Application");
            Entity internshipToAccept = DatabaseManager.getEntryById(INTERNSHIP_FILE,applicationToAccept.getArrayValueByIndex(2),"Internship");
            String applicationCompany = internshipToAccept.getArrayValueByIndex(8);

            String applicationStatus = applicationToAccept.getArrayValueByIndex(3);
            if (applicationStatus.equals("Approved")) {
                applicationToAccept.setArrayValueByIndex(3, "Accepted");
                DatabaseManager.updateEntry(APPLICATION_FILE, applicationId, applicationToAccept, "Application");
                String studentId = applicationToAccept.getArrayValueByIndex(1);
                Entity thisStudent = DatabaseManager.getEntryById(STUDENT_FILE,studentId,"Student");
                thisStudent.setArrayValueByIndex(9,applicationCompany);
                DatabaseManager.updateEntry(STUDENT_FILE,studentId,thisStudent,"Student");

            }

        } catch (Exception e) {
            System.err.println("ERROR IN ACCEPTING APPLICATION");
        }
    }
    private static String modifySlots(String value, int numberToAdd){
        int valueToChange = Integer.parseInt(value);
        valueToChange += numberToAdd;
        return Integer.toString(valueToChange);
    }

}
