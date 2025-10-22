package ims;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class StaffService {
    private final AppState state;

    public StaffService(AppState state) {
        this.state = state;
    }

    public List<CompanyRepresentative> listPendingRepresentatives() {
        return state.getCompanyReps().values().stream().filter(rep -> !rep.isApproved())
                .sorted(Comparator.comparing(CompanyRepresentative::getId)).collect(Collectors.toList());
    }

    public boolean approveRepresentative(String repId) throws IOException {
        CompanyRepresentative rep = state.getCompanyReps().get(repId);
        if (rep == null || rep.isApproved()) {
            return false;
        }
        rep.setApproved(true);
        state.getDataStore().saveCompanyReps(state.getCompanyReps().values());
        return true;
    }

    public boolean rejectRepresentative(String repId) throws IOException {
        CompanyRepresentative rep = state.getCompanyReps().remove(repId);
        if (rep == null) {
            return false;
        }
        state.getDataStore().saveCompanyReps(state.getCompanyReps().values());
        return true;
    }

    public List<Internship> listPendingInternships() {
        return state.getInternships().values().stream().filter(internship -> internship.getStatus() == InternshipStatus.PENDING)
                .sorted(Comparator.comparing(Internship::getTitle, String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList());
    }

    public boolean approveInternship(String internshipId) throws IOException {
        Internship internship = state.getInternships().get(internshipId);
        if (internship == null || internship.getStatus() != InternshipStatus.PENDING) {
            return false;
        }
        internship.setStatus(InternshipStatus.APPROVED);
        internship.setVisible(true);
        state.getDataStore().saveInternships(state.getInternships().values());
        return true;
    }

    public boolean rejectInternship(String internshipId) throws IOException {
        Internship internship = state.getInternships().get(internshipId);
        if (internship == null || internship.getStatus() != InternshipStatus.PENDING) {
            return false;
        }
        internship.setStatus(InternshipStatus.REJECTED);
        internship.setVisible(false);
        state.getDataStore().saveInternships(state.getInternships().values());
        return true;
    }

    public List<InternshipApplication> listWithdrawalRequests() {
        return state.getApplications().values().stream().filter(InternshipApplication::isWithdrawRequested)
                .sorted(Comparator.comparing(InternshipApplication::getId)).collect(Collectors.toList());
    }

    public boolean approveWithdrawal(String applicationId) throws IOException {
        InternshipApplication application = state.getApplications().get(applicationId);
        if (application == null || !application.isWithdrawRequested()) {
            return false;
        }
        application.setStatus(ApplicationStatus.WITHDRAWN);
        application.setWithdrawRequested(false);
        if (application.isStudentAccepted()) {
            Internship internship = state.getInternships().get(application.getInternshipId());
            if (internship != null && internship.getAcceptedCount() > 0) {
                internship.setAcceptedCount(internship.getAcceptedCount() - 1);
                if (internship.getStatus() == InternshipStatus.FILLED && internship.hasCapacity()) {
                    internship.setStatus(InternshipStatus.APPROVED);
                }
                state.getDataStore().saveInternships(state.getInternships().values());
            }
            application.setStudentAccepted(false);
        }
        state.getDataStore().saveApplications(state.getApplications().values());
        return true;
    }

    public boolean rejectWithdrawal(String applicationId) throws IOException {
        InternshipApplication application = state.getApplications().get(applicationId);
        if (application == null || !application.isWithdrawRequested()) {
            return false;
        }
        application.setWithdrawRequested(false);
        state.getDataStore().saveApplications(state.getApplications().values());
        return true;
    }

    public List<Internship> generateReport(Optional<InternshipStatus> statusFilter, Optional<String> majorFilter,
            Optional<InternshipLevel> levelFilter, Optional<LocalDate> closingBefore) {
        return state.getInternships().values().stream()
                .filter(internship -> statusFilter.map(status -> internship.getStatus() == status).orElse(true))
                .filter(internship -> majorFilter.map(major -> internship.getPreferredMajor() != null
                        && internship.getPreferredMajor().equalsIgnoreCase(major)).orElse(true))
                .filter(internship -> levelFilter.map(level -> internship.getLevel() == level).orElse(true))
                .filter(internship -> closingBefore
                        .map(date -> !internship.getClosingDate().isAfter(date)).orElse(true))
                .sorted(Comparator.comparing(Internship::getTitle, String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList());
    }

    public Map<String, Student> getStudents() {
        return state.getStudents();
    }

    public Map<String, Internship> getInternships() {
        return state.getInternships();
    }

    public Map<String, InternshipApplication> getApplications() {
        return state.getApplications();
    }
}
