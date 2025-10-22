package ims;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class StudentService {
    private final AppState state;

    public StudentService(AppState state) {
        this.state = state;
    }

    public List<Internship> listEligibleInternships(Student student, InternshipFilter filter, LocalDate today) {
        Map<String, Internship> internships = state.getInternships();
        return internships.values().stream()
                .filter(internship -> internship.getStatus() == InternshipStatus.APPROVED)
                .filter(Internship::isVisible)
                .filter(internship -> !today.isBefore(internship.getOpeningDate()))
                .filter(internship -> !today.isAfter(internship.getClosingDate()))
                .filter(internship -> internship.getPreferredMajor() == null
                        || internship.getPreferredMajor().isBlank()
                        || internship.getPreferredMajor().equalsIgnoreCase(student.getMajor()))
                .filter(internship -> isLevelEligible(student, internship.getLevel()))
                .filter(internship -> internship.hasCapacity())
                .filter(internship -> filter.getPreferredMajor().map(major -> internship.getPreferredMajor() != null
                        && internship.getPreferredMajor().equalsIgnoreCase(major)).orElse(true))
                .filter(internship -> filter.getLevel().map(level -> internship.getLevel() == level).orElse(true))
                .filter(internship -> filter.getStatus().map(status -> internship.getStatus() == status).orElse(true))
                .filter(internship -> filter.getClosingDateBefore()
                        .map(date -> !internship.getClosingDate().isAfter(date)).orElse(true))
                .sorted(Comparator.comparing(Internship::getTitle, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public boolean canApply(Student student, Internship internship, LocalDate today) {
        if (!internship.hasCapacity()) {
            return false;
        }
        if (today.isBefore(internship.getOpeningDate()) || today.isAfter(internship.getClosingDate())) {
            return false;
        }
        if (!isLevelEligible(student, internship.getLevel())) {
            return false;
        }
        long activeApplications = state.getApplications().values().stream()
                .filter(app -> app.getStudentId().equals(student.getId()))
                .filter(app -> app.getStatus() == ApplicationStatus.PENDING || app.getStatus() == ApplicationStatus.SUCCESSFUL)
                .count();
        if (activeApplications >= 3) {
            return false;
        }
        boolean alreadyApplied = state.getApplications().values().stream()
                .anyMatch(app -> app.getStudentId().equals(student.getId()) && app.getInternshipId().equals(internship.getId())
                        && app.getStatus() != ApplicationStatus.WITHDRAWN);
        if (alreadyApplied) {
            return false;
        }
        return internship.getPreferredMajor() == null || internship.getPreferredMajor().isBlank()
                || internship.getPreferredMajor().equalsIgnoreCase(student.getMajor());
    }

    public Optional<InternshipApplication> apply(Student student, Internship internship) throws IOException {
        if (!canApply(student, internship, LocalDate.now())) {
            return Optional.empty();
        }
        String appId = state.getDataStore().nextApplicationId();
        InternshipApplication application = new InternshipApplication(appId, student.getId(), internship.getId(),
                ApplicationStatus.PENDING, false, false);
        state.getApplications().put(appId, application);
        state.getDataStore().saveApplications(state.getApplications().values());
        return Optional.of(application);
    }

    public List<InternshipApplication> listApplications(Student student) {
        return state.getApplications().values().stream()
                .filter(app -> app.getStudentId().equals(student.getId()))
                .sorted(Comparator.comparing(InternshipApplication::getId))
                .collect(Collectors.toList());
    }

    public boolean requestWithdrawal(Student student, String applicationId) throws IOException {
        InternshipApplication application = state.getApplications().get(applicationId);
        if (application == null || !application.getStudentId().equals(student.getId())) {
            return false;
        }
        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            return false;
        }
        application.setWithdrawRequested(true);
        state.getDataStore().saveApplications(state.getApplications().values());
        return true;
    }

    public boolean acceptOffer(Student student, String applicationId) throws IOException {
        InternshipApplication application = state.getApplications().get(applicationId);
        if (application == null || !application.getStudentId().equals(student.getId())) {
            return false;
        }
        if (application.getStatus() != ApplicationStatus.SUCCESSFUL || application.isStudentAccepted()) {
            return false;
        }
        Internship internship = state.getInternships().get(application.getInternshipId());
        if (internship == null) {
            return false;
        }
        application.setStudentAccepted(true);
        internship.setAcceptedCount(internship.getAcceptedCount() + 1);
        if (internship.getAcceptedCount() >= internship.getSlots()) {
            internship.setStatus(InternshipStatus.FILLED);
        } else if (internship.getStatus() == InternshipStatus.FILLED) {
            internship.setStatus(InternshipStatus.APPROVED);
        }
        withdrawOtherApplications(student, applicationId);
        state.getDataStore().saveApplications(state.getApplications().values());
        state.getDataStore().saveInternships(state.getInternships().values());
        return true;
    }

    private void withdrawOtherApplications(Student student, String acceptedApplicationId) {
        for (InternshipApplication app : state.getApplications().values()) {
            if (app.getStudentId().equals(student.getId()) && !app.getId().equals(acceptedApplicationId)) {
                if (app.getStatus() == ApplicationStatus.PENDING || app.getStatus() == ApplicationStatus.SUCCESSFUL) {
                    app.setStatus(ApplicationStatus.WITHDRAWN);
                    app.setWithdrawRequested(false);
                    app.setStudentAccepted(false);
                }
            }
        }
    }

    private boolean isLevelEligible(Student student, InternshipLevel level) {
        return switch (level) {
            case BASIC -> true;
            case INTERMEDIATE, ADVANCED -> student.getYearOfStudy() >= 3;
        };
    }
}
