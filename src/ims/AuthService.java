package ims;

import java.io.IOException;
import java.util.Optional;

public class AuthService {
    private final AppState state;

    public AuthService(AppState state) {
        this.state = state;
    }

    public Optional<User> login(String userId, String password) {
        User user = findUserById(userId);
        if (user != null && user.verifyPassword(password)) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public User findUserById(String userId) {
        if (state.getStudents().containsKey(userId)) {
            return state.getStudents().get(userId);
        }
        if (state.getCompanyReps().containsKey(userId)) {
            return state.getCompanyReps().get(userId);
        }
        if (state.getStaff().containsKey(userId)) {
            return state.getStaff().get(userId);
        }
        return null;
    }

    public Optional<CompanyRepresentative> registerCompanyRep(String emailId, String name, String companyName,
            String department, String position) throws IOException {
        if (state.getCompanyReps().containsKey(emailId) || state.getStaff().containsKey(emailId)
                || state.getStudents().containsKey(emailId)) {
            return Optional.empty();
        }
        CompanyRepresentative rep = new CompanyRepresentative(emailId, name, DataStore.DEFAULT_PASSWORD, companyName,
                department, position, emailId, false);
        state.getCompanyReps().put(emailId, rep);
        state.getDataStore().saveCompanyReps(state.getCompanyReps().values());
        return Optional.of(rep);
    }

    public void changePassword(User user, String newPassword) throws IOException {
        user.setPassword(newPassword);
        switch (user.getRole()) {
            case STUDENT -> state.getDataStore().saveStudents(state.getStudents().values());
            case COMPANY_REPRESENTATIVE -> state.getDataStore().saveCompanyReps(state.getCompanyReps().values());
            case CAREER_STAFF -> state.getDataStore().saveStaff(state.getStaff().values());
            default -> {
            }
        }
    }
}
