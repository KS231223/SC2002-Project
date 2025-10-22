package ims;

public class InternshipApplication {
    private final String id;
    private final String studentId;
    private final String internshipId;
    private ApplicationStatus status;
    private boolean withdrawRequested;
    private boolean studentAccepted;

    public InternshipApplication(String id, String studentId, String internshipId, ApplicationStatus status, boolean withdrawRequested, boolean studentAccepted) {
        this.id = id;
        this.studentId = studentId;
        this.internshipId = internshipId;
        this.status = status;
        this.withdrawRequested = withdrawRequested;
        this.studentAccepted = studentAccepted;
    }

    public String getId() {
        return id;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getInternshipId() {
        return internshipId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public boolean isWithdrawRequested() {
        return withdrawRequested;
    }

    public void setWithdrawRequested(boolean withdrawRequested) {
        this.withdrawRequested = withdrawRequested;
    }

    public boolean isStudentAccepted() {
        return studentAccepted;
    }

    public void setStudentAccepted(boolean studentAccepted) {
        this.studentAccepted = studentAccepted;
    }
}
