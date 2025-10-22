package ims;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final Scanner scanner = new Scanner(System.in);
    private final AppState state = new AppState();
    private final Map<String, InternshipFilter> filters = new HashMap<>();
    private AuthService authService;
    private StudentService studentService;
    private CompanyRepService companyService;
    private StaffService staffService;

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        try {
            state.load();
            authService = new AuthService(state);
            studentService = new StudentService(state);
            companyService = new CompanyRepService(state);
            staffService = new StaffService(state);
            mainMenu();
        } catch (IOException e) {
            System.out.println("Failed to initialize application: " + e.getMessage());
        }
    }

    private void mainMenu() {
        while (true) {
            System.out.println();
            System.out.println("=== Internship Placement Management System ===");
            System.out.println("1. Login");
            System.out.println("2. Register as Company Representative");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            int choice = readInt();
            switch (choice) {
                case 1 -> handleLogin();
                case 2 -> handleRegistration();
                case 3 -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void handleLogin() {
        System.out.print("User ID: ");
        String userId = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        Optional<User> user = authService.login(userId, password);
        if (user.isEmpty()) {
            System.out.println("Invalid credentials.");
            return;
        }
        User loggedIn = user.get();
        if (loggedIn instanceof CompanyRepresentative rep && !rep.isApproved()) {
            System.out.println("Account pending approval by Career Center Staff.");
            return;
        }
        switch (loggedIn.getRole()) {
            case STUDENT -> studentMenu((Student) loggedIn);
            case COMPANY_REPRESENTATIVE -> companyMenu((CompanyRepresentative) loggedIn);
            case CAREER_STAFF -> staffMenu((CareerCenterStaff) loggedIn);
            default -> System.out.println("Unknown user role.");
        }
    }

    private void handleRegistration() {
        try {
            System.out.println();
            System.out.println("=== Company Representative Registration ===");
            System.out.print("Company email (will be your login ID): ");
            String email = scanner.nextLine().trim();
            System.out.print("Full name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Company name: ");
            String companyName = scanner.nextLine().trim();
            System.out.print("Department: ");
            String department = scanner.nextLine().trim();
            System.out.print("Position: ");
            String position = scanner.nextLine().trim();
            Optional<CompanyRepresentative> rep = authService.registerCompanyRep(email, name, companyName, department,
                    position);
            if (rep.isPresent()) {
                System.out.println(
                        "Registration submitted. You can login once a Career Center Staff approves your account.");
            } else {
                System.out.println("Registration failed. Account may already exist.");
            }
        } catch (IOException e) {
            System.out.println("Failed to register: " + e.getMessage());
        }
    }

    private void studentMenu(Student student) {
        InternshipFilter filter = filters.computeIfAbsent(student.getId(), key -> new InternshipFilter());
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("=== Student Menu ===");
            System.out.println("1. View available internships");
            System.out.println("2. Update internship filters");
            System.out.println("3. Clear internship filters");
            System.out.println("4. Apply for an internship");
            System.out.println("5. View my applications");
            System.out.println("6. Request withdrawal");
            System.out.println("7. Accept internship offer");
            System.out.println("8. Change password");
            System.out.println("9. Logout");
            System.out.print("Choose an option: ");
            int choice = readInt();
            try {
                switch (choice) {
                    case 1 -> displayInternships(student, filter);
                    case 2 -> updateFilter(filter);
                    case 3 -> {
                        filter.clear();
                        System.out.println("Filters cleared.");
                    }
                    case 4 -> applyForInternship(student);
                    case 5 -> displayApplications(student);
                    case 6 -> requestWithdrawal(student);
                    case 7 -> acceptOffer(student);
                    case 8 -> changePassword(student);
                    case 9 -> running = false;
                    default -> System.out.println("Invalid option.");
                }
            } catch (IOException e) {
                System.out.println("Operation failed: " + e.getMessage());
            }
        }
    }

    private void companyMenu(CompanyRepresentative rep) {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("=== Company Representative Menu ===");
            System.out.println("1. List my internships");
            System.out.println("2. Create internship opportunity");
            System.out.println("3. Toggle internship visibility");
            System.out.println("4. View internship applications");
            System.out.println("5. Approve student application");
            System.out.println("6. Reject student application");
            System.out.println("7. Change password");
            System.out.println("8. Logout");
            System.out.print("Choose an option: ");
            int choice = readInt();
            try {
                switch (choice) {
                    case 1 -> listCompanyInternships(rep);
                    case 2 -> createInternship(rep);
                    case 3 -> toggleVisibility(rep);
                    case 4 -> viewInternshipApplications(rep);
                    case 5 -> approveApplication(rep);
                    case 6 -> rejectApplication(rep);
                    case 7 -> changePassword(rep);
                    case 8 -> running = false;
                    default -> System.out.println("Invalid option.");
                }
            } catch (IOException e) {
                System.out.println("Operation failed: " + e.getMessage());
            }
        }
    }

    private void staffMenu(CareerCenterStaff staff) {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("=== Career Center Staff Menu ===");
            System.out.println("1. Review company representative registrations");
            System.out.println("2. Review internship submissions");
            System.out.println("3. Handle withdrawal requests");
            System.out.println("4. Generate internship report");
            System.out.println("5. Change password");
            System.out.println("6. Logout");
            System.out.print("Choose an option: ");
            int choice = readInt();
            try {
                switch (choice) {
                    case 1 -> manageRepresentativeRequests();
                    case 2 -> manageInternshipSubmissions();
                    case 3 -> manageWithdrawals();
                    case 4 -> generateReport();
                    case 5 -> changePassword(staff);
                    case 6 -> running = false;
                    default -> System.out.println("Invalid option.");
                }
            } catch (IOException e) {
                System.out.println("Operation failed: " + e.getMessage());
            }
        }
    }

    private void displayInternships(Student student, InternshipFilter filter) {
        List<Internship> internships = studentService.listEligibleInternships(student, filter, LocalDate.now());
        if (internships.isEmpty()) {
            System.out.println("No internships found for current filters.");
            return;
        }
        System.out.println("--- Available Internships ---");
        for (Internship internship : internships) {
            System.out.printf("%s | %s | %s | Level: %s | Slots: %d/%d | Closes: %s | Visible: %s%n",
                    internship.getId(), internship.getTitle(), internship.getCompanyName(), internship.getLevel(),
                    internship.getAcceptedCount(), internship.getSlots(),
                    internship.getClosingDate().format(DATE_FORMAT), internship.isVisible() ? "Yes" : "No");
            System.out.printf("Preferred Major: %s%n", internship.getPreferredMajor());
            System.out.printf("Description: %s%n", internship.getDescription());
            System.out.println();
        }
    }

    private void displayApplications(Student student) {
        List<InternshipApplication> applications = studentService.listApplications(student);
        if (applications.isEmpty()) {
            System.out.println("No applications found.");
            return;
        }
        System.out.println("--- My Applications ---");
        for (InternshipApplication app : applications) {
            Internship internship = state.getInternships().get(app.getInternshipId());
            String internshipInfo = internship == null ? "[Internship unavailable]"
                    : internship.getTitle() + " (" + internship.getCompanyName() + ")";
            System.out.printf("%s | %s | Status: %s | Withdraw requested: %s | Accepted: %s%n", app.getId(),
                    internshipInfo, app.getStatus(), booleanLabel(app.isWithdrawRequested()),
                    booleanLabel(app.isStudentAccepted()));
        }
    }

    private void applyForInternship(Student student) throws IOException {
        System.out.print("Enter internship ID to apply: ");
        String internshipId = scanner.nextLine().trim();
        Internship internship = state.getInternships().get(internshipId);
        if (internship == null) {
            System.out.println("Internship not found.");
            return;
        }
        if (!studentService.canApply(student, internship, LocalDate.now())) {
            System.out.println("You are not eligible to apply or limit reached.");
            return;
        }
        Optional<InternshipApplication> application = studentService.apply(student, internship);
        System.out.println(application.isPresent() ? "Application submitted." : "Unable to apply.");
    }

    private void requestWithdrawal(Student student) throws IOException {
        System.out.print("Enter application ID to request withdrawal: ");
        String applicationId = scanner.nextLine().trim();
        boolean success = studentService.requestWithdrawal(student, applicationId);
        System.out.println(success ? "Withdrawal request submitted." : "Unable to submit withdrawal request.");
    }

    private void acceptOffer(Student student) throws IOException {
        System.out.print("Enter application ID to accept: ");
        String applicationId = scanner.nextLine().trim();
        boolean accepted = studentService.acceptOffer(student, applicationId);
        System.out.println(accepted ? "Offer accepted." : "Unable to accept offer.");
    }

    private void updateFilter(InternshipFilter filter) {
        System.out.print("Filter by status (leave blank to skip) [PENDING/APPROVED/REJECTED/FILLED]: ");
        String statusInput = scanner.nextLine().trim();
        if (statusInput.isBlank()) {
            filter.setStatus(null);
        } else {
            try {
                filter.setStatus(InternshipStatus.fromString(statusInput));
            } catch (IllegalArgumentException ex) {
                System.out.println("Invalid status; keeping previous value.");
            }
        }
        System.out.print("Filter by preferred major (leave blank to skip): ");
        filter.setPreferredMajor(scanner.nextLine());
        System.out.print("Filter by level (leave blank to skip) [BASIC/INTERMEDIATE/ADVANCED]: ");
        String levelInput = scanner.nextLine().trim();
        if (levelInput.isBlank()) {
            filter.setLevel(null);
        } else {
            try {
                filter.setLevel(InternshipLevel.fromString(levelInput));
            } catch (IllegalArgumentException ex) {
                System.out.println("Invalid level; keeping previous value.");
            }
        }
        System.out.print("Filter by closing date (YYYY-MM-DD, blank to skip): ");
        String dateInput = scanner.nextLine().trim();
        if (dateInput.isBlank()) {
            filter.setClosingDateBefore(null);
        } else {
            try {
                filter.setClosingDateBefore(LocalDate.parse(dateInput, DATE_FORMAT));
            } catch (Exception ex) {
                System.out.println("Invalid date; keeping previous value.");
            }
        }
        System.out.println("Filters updated.");
    }

    private void listCompanyInternships(CompanyRepresentative rep) {
        List<Internship> internships = companyService.listInternships(rep);
        if (internships.isEmpty()) {
            System.out.println("No internships found.");
            return;
        }
        System.out.println("--- My Internships ---");
        for (Internship internship : internships) {
            System.out.printf("%s | %s | Status: %s | Visible: %s | Slots: %d/%d | %s - %s%n", internship.getId(),
                    internship.getTitle(), internship.getStatus(), booleanLabel(internship.isVisible()),
                    internship.getAcceptedCount(), internship.getSlots(),
                    internship.getOpeningDate().format(DATE_FORMAT), internship.getClosingDate().format(DATE_FORMAT));
        }
    }

    private void createInternship(CompanyRepresentative rep) throws IOException {
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Description: ");
        String description = scanner.nextLine().trim();
        System.out.print("Level [BASIC/INTERMEDIATE/ADVANCED]: ");
        InternshipLevel level;
        try {
            level = InternshipLevel.fromString(scanner.nextLine().trim());
        } catch (IllegalArgumentException ex) {
            System.out.println("Invalid level.");
            return;
        }
        System.out.print("Preferred major (leave blank for any): ");
        String major = scanner.nextLine().trim();
        System.out.print("Application opening date (YYYY-MM-DD): ");
        String opening = scanner.nextLine().trim();
        System.out.print("Application closing date (YYYY-MM-DD): ");
        String closing = scanner.nextLine().trim();
        System.out.print("Number of slots (1-10): ");
        int slots = readInt();
        Optional<Internship> internship = companyService.createInternship(rep, title, description, level,
                major.isBlank() ? null : major, opening, closing, slots);
        if (internship.isPresent()) {
            System.out.println("Internship submitted for approval.");
        } else {
            System.out.println("Unable to create internship. Check limits and dates.");
        }
    }

    private void toggleVisibility(CompanyRepresentative rep) throws IOException {
        System.out.print("Enter internship ID: ");
        String internshipId = scanner.nextLine().trim();
        Internship internship = state.getInternships().get(internshipId);
        if (internship == null) {
            System.out.println("Internship not found.");
            return;
        }
        System.out.print("Set visibility to (true/false): ");
        boolean visible = Boolean.parseBoolean(scanner.nextLine().trim());
        if (companyService.toggleVisibility(rep, internshipId, visible)) {
            System.out.println("Visibility updated.");
        } else {
            System.out.println("Unable to update visibility.");
        }
    }

    private void viewInternshipApplications(CompanyRepresentative rep) {
        System.out.print("Enter internship ID: ");
        String internshipId = scanner.nextLine().trim();
        Internship internship = state.getInternships().get(internshipId);
        if (internship == null || !internship.getRepresentativeId().equals(rep.getId())) {
            System.out.println("Internship not found or access denied.");
            return;
        }
        List<InternshipApplication> applications = companyService.listApplications(internshipId);
        if (applications.isEmpty()) {
            System.out.println("No applications.");
            return;
        }
        System.out.println("--- Applications ---");
        for (InternshipApplication app : applications) {
            Student applicant = state.getStudents().get(app.getStudentId());
            String applicantInfo = applicant == null ? app.getStudentId()
                    : applicant.getName() + " (" + applicant.getMajor() + ", Year " + applicant.getYearOfStudy() + ")";
            System.out.printf("%s | %s | Status: %s | Withdraw requested: %s | Accepted: %s%n", app.getId(),
                    applicantInfo, app.getStatus(), booleanLabel(app.isWithdrawRequested()),
                    booleanLabel(app.isStudentAccepted()));
        }
    }

    private void approveApplication(CompanyRepresentative rep) throws IOException {
        System.out.print("Application ID to approve: ");
        String applicationId = scanner.nextLine().trim();
        boolean success = companyService.approveApplication(rep, applicationId);
        System.out.println(success ? "Application approved." : "Unable to approve application.");
    }

    private void rejectApplication(CompanyRepresentative rep) throws IOException {
        System.out.print("Application ID to reject: ");
        String applicationId = scanner.nextLine().trim();
        boolean success = companyService.rejectApplication(rep, applicationId);
        System.out.println(success ? "Application rejected." : "Unable to reject application.");
    }

    private void manageRepresentativeRequests() throws IOException {
        List<CompanyRepresentative> pending = staffService.listPendingRepresentatives();
        if (pending.isEmpty()) {
            System.out.println("No pending registrations.");
            return;
        }
        System.out.println("--- Pending Representatives ---");
        pending.forEach(rep -> System.out.printf("%s | %s | %s | %s%n", rep.getId(), rep.getName(), rep.getCompanyName(),
                rep.getEmail()));
        System.out.print("Enter representative ID to approve/reject (blank to cancel): ");
        String choice = scanner.nextLine().trim();
        if (choice.isBlank()) {
            return;
        }
        System.out.print("Approve? (yes/no): ");
        String decision = scanner.nextLine().trim().toLowerCase();
        boolean success;
        if (decision.startsWith("y")) {
            success = staffService.approveRepresentative(choice);
        } else {
            success = staffService.rejectRepresentative(choice);
        }
        System.out.println(success ? "Action completed." : "Unable to process selection.");
    }

    private void manageInternshipSubmissions() throws IOException {
        List<Internship> pending = staffService.listPendingInternships();
        if (pending.isEmpty()) {
            System.out.println("No pending internships.");
            return;
        }
        System.out.println("--- Pending Internships ---");
        for (Internship internship : pending) {
            System.out.printf("%s | %s | Company: %s | Level: %s | Slots: %d%n", internship.getId(),
                    internship.getTitle(), internship.getCompanyName(), internship.getLevel(), internship.getSlots());
        }
        System.out.print("Enter internship ID to approve/reject (blank to cancel): ");
        String choice = scanner.nextLine().trim();
        if (choice.isBlank()) {
            return;
        }
        System.out.print("Approve? (yes/no): ");
        String decision = scanner.nextLine().trim().toLowerCase();
        boolean success = decision.startsWith("y") ? staffService.approveInternship(choice)
                : staffService.rejectInternship(choice);
        System.out.println(success ? "Action completed." : "Unable to process selection.");
    }

    private void manageWithdrawals() throws IOException {
        List<InternshipApplication> requests = staffService.listWithdrawalRequests();
        if (requests.isEmpty()) {
            System.out.println("No withdrawal requests.");
            return;
        }
        System.out.println("--- Withdrawal Requests ---");
        for (InternshipApplication app : requests) {
            Student student = state.getStudents().get(app.getStudentId());
            Internship internship = state.getInternships().get(app.getInternshipId());
            String studentInfo = student == null ? app.getStudentId()
                    : student.getName() + " (" + student.getMajor() + ")";
            String internshipInfo = internship == null ? app.getInternshipId()
                    : internship.getTitle() + " (" + internship.getCompanyName() + ")";
            System.out.printf("%s | %s | %s | Status: %s%n", app.getId(), studentInfo, internshipInfo,
                    app.getStatus());
        }
        System.out.print("Enter application ID to approve/reject (blank to cancel): ");
        String choice = scanner.nextLine().trim();
        if (choice.isBlank()) {
            return;
        }
        System.out.print("Approve withdrawal? (yes/no): ");
        String decision = scanner.nextLine().trim().toLowerCase();
        boolean success = decision.startsWith("y") ? staffService.approveWithdrawal(choice)
                : staffService.rejectWithdrawal(choice);
        System.out.println(success ? "Action completed." : "Unable to process selection.");
    }

    private void generateReport() {
        System.out.print("Filter by status (blank for any): ");
        Optional<InternshipStatus> statusFilter = parseStatus(scanner.nextLine().trim());
        System.out.print("Filter by preferred major (blank for any): ");
        String majorInput = scanner.nextLine().trim();
        Optional<String> majorFilter = majorInput.isBlank() ? Optional.empty() : Optional.of(majorInput);
        System.out.print("Filter by level (blank for any): ");
        Optional<InternshipLevel> levelFilter = parseLevel(scanner.nextLine().trim());
        System.out.print("Filter by closing date on/before (YYYY-MM-DD, blank for any): ");
        String dateInput = scanner.nextLine().trim();
        Optional<LocalDate> closingFilter = Optional.empty();
        if (!dateInput.isBlank()) {
            try {
                closingFilter = Optional.of(LocalDate.parse(dateInput, DATE_FORMAT));
            } catch (Exception ex) {
                System.out.println("Invalid date. Ignoring.");
            }
        }
        List<Internship> report = staffService.generateReport(statusFilter, majorFilter, levelFilter, closingFilter);
        if (report.isEmpty()) {
            System.out.println("No internships match the filters.");
            return;
        }
        System.out.println("--- Internship Report ---");
        for (Internship internship : report) {
            System.out.printf("%s | %s | %s | Level: %s | Status: %s | Preferred Major: %s | Close: %s%n",
                    internship.getId(), internship.getTitle(), internship.getCompanyName(), internship.getLevel(),
                    internship.getStatus(), internship.getPreferredMajor(),
                    internship.getClosingDate().format(DATE_FORMAT));
        }
    }

    private Optional<InternshipStatus> parseStatus(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(InternshipStatus.fromString(input));
        } catch (IllegalArgumentException ex) {
            System.out.println("Invalid status. Ignoring.");
            return Optional.empty();
        }
    }

    private Optional<InternshipLevel> parseLevel(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(InternshipLevel.fromString(input));
        } catch (IllegalArgumentException ex) {
            System.out.println("Invalid level. Ignoring.");
            return Optional.empty();
        }
    }

    private void changePassword(User user) throws IOException {
        System.out.print("Enter current password: ");
        String current = scanner.nextLine().trim();
        if (!user.verifyPassword(current)) {
            System.out.println("Incorrect current password.");
            return;
        }
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine().trim();
        if (newPassword.isBlank()) {
            System.out.println("Password cannot be blank.");
            return;
        }
        System.out.print("Confirm new password: ");
        String confirm = scanner.nextLine().trim();
        if (!newPassword.equals(confirm)) {
            System.out.println("Passwords do not match.");
            return;
        }
        authService.changePassword(user, newPassword);
        System.out.println("Password updated.");
    }

    private int readInt() {
        while (true) {
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                System.out.print("Enter a number: ");
            }
        }
    }

    private String booleanLabel(boolean value) {
        return value ? "Yes" : "No";
    }
}
