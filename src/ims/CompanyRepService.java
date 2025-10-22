package ims;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CompanyRepService {
    private static final int MAX_INTERNSHIPS_PER_REP = 5;
    private static final int MAX_SLOTS = 10;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final AppState state;

    public CompanyRepService(AppState state) {
        this.state = state;
    }

    public List<Internship> listInternships(CompanyRepresentative rep) {
        return state.getInternships().values().stream()
                .filter(internship -> internship.getRepresentativeId().equals(rep.getId()))
                .sorted((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()))
                .collect(Collectors.toList());
    }

    public Optional<Internship> createInternship(CompanyRepresentative rep, String title, String description,
            InternshipLevel level, String preferredMajor, String openingDateStr, String closingDateStr, int slots)
            throws IOException {
        if (countInternships(rep) >= MAX_INTERNSHIPS_PER_REP) {
            return Optional.empty();
        }
        if (slots <= 0 || slots > MAX_SLOTS) {
            return Optional.empty();
        }
        try {
            LocalDate opening = LocalDate.parse(openingDateStr, DATE_FORMAT);
            LocalDate closing = LocalDate.parse(closingDateStr, DATE_FORMAT);
            if (closing.isBefore(opening)) {
                return Optional.empty();
            }
            String id = state.getDataStore().nextInternshipId(state.getInternships());
            Internship internship = new Internship(id, title, description, level, preferredMajor, opening, closing,
                    InternshipStatus.PENDING, rep.getCompanyName(), rep.getId(), false, slots, 0);
            state.getInternships().put(id, internship);
            state.getDataStore().saveInternships(state.getInternships().values());
            return Optional.of(internship);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private long countInternships(CompanyRepresentative rep) {
        return state.getInternships().values().stream()
                .filter(internship -> internship.getRepresentativeId().equals(rep.getId()))
                .count();
    }

    public boolean toggleVisibility(CompanyRepresentative rep, String internshipId, boolean visible) throws IOException {
        Internship internship = state.getInternships().get(internshipId);
        if (internship == null || !internship.getRepresentativeId().equals(rep.getId())) {
            return false;
        }
        internship.setVisible(visible);
        state.getDataStore().saveInternships(state.getInternships().values());
        return true;
    }

    public List<InternshipApplication> listApplications(String internshipId) {
        Map<String, InternshipApplication> applications = state.getApplications();
        return applications.values().stream().filter(app -> app.getInternshipId().equals(internshipId))
                .sorted((a, b) -> a.getId().compareToIgnoreCase(b.getId())).collect(Collectors.toCollection(ArrayList::new));
    }

    public boolean approveApplication(CompanyRepresentative rep, String applicationId) throws IOException {
        InternshipApplication application = state.getApplications().get(applicationId);
        if (application == null) {
            return false;
        }
        Internship internship = state.getInternships().get(application.getInternshipId());
        if (internship == null || !internship.getRepresentativeId().equals(rep.getId())) {
            return false;
        }
        if (!internship.hasCapacity() && application.getStatus() != ApplicationStatus.SUCCESSFUL) {
            return false;
        }
        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            return false;
        }
        if (application.getStatus() == ApplicationStatus.SUCCESSFUL) {
            return true;
        }
        application.setStatus(ApplicationStatus.SUCCESSFUL);
        application.setWithdrawRequested(false);
        application.setStudentAccepted(false);
        state.getDataStore().saveApplications(state.getApplications().values());
        return true;
    }

    public boolean rejectApplication(CompanyRepresentative rep, String applicationId) throws IOException {
        InternshipApplication application = state.getApplications().get(applicationId);
        if (application == null) {
            return false;
        }
        Internship internship = state.getInternships().get(application.getInternshipId());
        if (internship == null || !internship.getRepresentativeId().equals(rep.getId())) {
            return false;
        }
        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            return false;
        }
        application.setStatus(ApplicationStatus.UNSUCCESSFUL);
        application.setWithdrawRequested(false);
        application.setStudentAccepted(false);
        state.getDataStore().saveApplications(state.getApplications().values());
        return true;
    }
}
