package ims;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DataStore {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    public static final String DEFAULT_PASSWORD = "password";

    private final Path baseDir;
    private final Path dataDir;
    private final Path studentsFile;
    private final Path staffFile;
    private final Path repsFile;
    private final Path internshipsFile;
    private final Path applicationsFile;

    public DataStore(Path baseDir) {
        this.baseDir = baseDir;
        this.dataDir = baseDir.resolve("data");
        this.studentsFile = dataDir.resolve("students.csv");
        this.staffFile = dataDir.resolve("staff.csv");
        this.repsFile = dataDir.resolve("company_reps.csv");
        this.internshipsFile = dataDir.resolve("internships.csv");
        this.applicationsFile = dataDir.resolve("applications.csv");
    }

    public void initialize() throws IOException {
        Files.createDirectories(dataDir);
        seedStudents();
        seedStaff();
        ensureFileWithHeader(repsFile, List.of("RepID,Name,Password,CompanyName,Department,Position,Email,Approved"));
        ensureFileWithHeader(internshipsFile, List.of(
                "InternshipID,Title,Description,Level,PreferredMajor,OpeningDate,ClosingDate,Status,CompanyName,RepresentativeId,Visible,Slots,AcceptedCount"));
        ensureFileWithHeader(applicationsFile, List.of(
                "ApplicationID,StudentID,InternshipID,Status,WithdrawRequested,StudentAccepted"));
    }

    private void seedStudents() throws IOException {
        if (Files.exists(studentsFile)) {
            return;
        }
        Path sample = baseDir.resolve("sample_student_list.csv");
        List<String> lines = Files.exists(sample) ? Files.readAllLines(sample, StandardCharsets.UTF_8) : List.of();
        List<String> output = new ArrayList<>();
        output.add("StudentID,Name,Password,Major,Year,Email");
        for (int i = 1; i < lines.size(); i++) {
            List<String> tokens = CsvUtils.parseLine(lines.get(i));
            if (tokens.size() < 5) {
                continue;
            }
            String id = tokens.get(0).trim();
            String name = tokens.get(1).trim();
            String major = tokens.get(2).trim();
            int year = Integer.parseInt(tokens.get(3).trim());
            String email = tokens.get(4).trim();
            output.add(CsvUtils.toCsv(List.of(id, name, DEFAULT_PASSWORD, major, Integer.toString(year), email)));
        }
        Files.write(studentsFile, output, StandardCharsets.UTF_8);
    }

    private void seedStaff() throws IOException {
        if (Files.exists(staffFile)) {
            return;
        }
        Path sample = baseDir.resolve("sample_staff_list.csv");
        List<String> lines = Files.exists(sample) ? Files.readAllLines(sample, StandardCharsets.UTF_8) : List.of();
        List<String> output = new ArrayList<>();
        output.add("StaffID,Name,Password,Role,Department,Email");
        for (int i = 1; i < lines.size(); i++) {
            List<String> tokens = CsvUtils.parseLine(lines.get(i));
            if (tokens.size() < 5) {
                continue;
            }
            String id = tokens.get(0).trim();
            String name = tokens.get(1).trim();
            String role = tokens.get(2).trim();
            String department = tokens.get(3).trim();
            String email = tokens.get(4).trim();
            output.add(CsvUtils.toCsv(List.of(id, name, DEFAULT_PASSWORD, role, department, email)));
        }
        Files.write(staffFile, output, StandardCharsets.UTF_8);
    }

    private void ensureFileWithHeader(Path path, List<String> header) throws IOException {
        if (!Files.exists(path)) {
            Files.write(path, header, StandardCharsets.UTF_8);
        }
    }

    public Map<String, Student> loadStudents() throws IOException {
        Map<String, Student> students = new HashMap<>();
        List<String> lines = Files.readAllLines(studentsFile, StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) {
            List<String> tokens = CsvUtils.parseLine(lines.get(i));
            if (tokens.size() < 6) {
                continue;
            }
            String id = tokens.get(0);
            String name = tokens.get(1);
            String password = tokens.get(2);
            String major = tokens.get(3);
            int year = Integer.parseInt(tokens.get(4));
            String email = tokens.get(5);
            students.put(id, new Student(id, name, password, major, year, email));
        }
        return students;
    }

    public void saveStudents(Collection<Student> students) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("StudentID,Name,Password,Major,Year,Email");
        for (Student student : students) {
            lines.add(CsvUtils.toCsv(List.of(
                    student.getId(),
                    student.getName(),
                    student.getPassword(),
                    student.getMajor(),
                    Integer.toString(student.getYearOfStudy()),
                    student.getEmail())));
        }
        Files.write(studentsFile, lines, StandardCharsets.UTF_8);
    }

    public Map<String, CareerCenterStaff> loadStaff() throws IOException {
        Map<String, CareerCenterStaff> staff = new HashMap<>();
        List<String> lines = Files.readAllLines(staffFile, StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) {
            List<String> tokens = CsvUtils.parseLine(lines.get(i));
            if (tokens.size() < 6) {
                continue;
            }
            String id = tokens.get(0);
            String name = tokens.get(1);
            String password = tokens.get(2);
            String department = tokens.get(4);
            String email = tokens.get(5);
            staff.put(id, new CareerCenterStaff(id, name, password, department, email));
        }
        return staff;
    }

    public void saveStaff(Collection<CareerCenterStaff> staff) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("StaffID,Name,Password,Role,Department,Email");
        for (CareerCenterStaff member : staff) {
            lines.add(CsvUtils.toCsv(List.of(
                    member.getId(),
                    member.getName(),
                    member.getPassword(),
                    "Career Center Staff",
                    member.getDepartment(),
                    member.getEmail())));
        }
        Files.write(staffFile, lines, StandardCharsets.UTF_8);
    }

    public Map<String, CompanyRepresentative> loadCompanyReps() throws IOException {
        Map<String, CompanyRepresentative> reps = new HashMap<>();
        List<String> lines = Files.readAllLines(repsFile, StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) {
            List<String> tokens = CsvUtils.parseLine(lines.get(i));
            if (tokens.size() < 8) {
                continue;
            }
            String id = tokens.get(0);
            String name = tokens.get(1);
            String password = tokens.get(2);
            String companyName = tokens.get(3);
            String department = tokens.get(4);
            String position = tokens.get(5);
            String email = tokens.get(6);
            boolean approved = Boolean.parseBoolean(tokens.get(7));
            reps.put(id, new CompanyRepresentative(id, name, password, companyName, department, position, email, approved));
        }
        return reps;
    }

    public void saveCompanyReps(Collection<CompanyRepresentative> reps) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("RepID,Name,Password,CompanyName,Department,Position,Email,Approved");
        for (CompanyRepresentative rep : reps) {
            lines.add(CsvUtils.toCsv(List.of(
                    rep.getId(),
                    rep.getName(),
                    rep.getPassword(),
                    rep.getCompanyName(),
                    rep.getDepartment(),
                    rep.getPosition(),
                    rep.getEmail(),
                    Boolean.toString(rep.isApproved()))));
        }
        Files.write(repsFile, lines, StandardCharsets.UTF_8);
    }

    public Map<String, Internship> loadInternships() throws IOException {
        Map<String, Internship> internships = new HashMap<>();
        if (!Files.exists(internshipsFile)) {
            return internships;
        }
        List<String> lines = Files.readAllLines(internshipsFile, StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) {
            List<String> tokens = CsvUtils.parseLine(lines.get(i));
            if (tokens.size() < 13) {
                continue;
            }
            String id = tokens.get(0);
            String title = tokens.get(1);
            String description = tokens.get(2);
            InternshipLevel level = InternshipLevel.fromString(tokens.get(3));
            String preferredMajor = tokens.get(4);
            LocalDate opening = LocalDate.parse(tokens.get(5), DATE_FORMAT);
            LocalDate closing = LocalDate.parse(tokens.get(6), DATE_FORMAT);
            InternshipStatus status = InternshipStatus.fromString(tokens.get(7));
            String companyName = tokens.get(8);
            String repId = tokens.get(9);
            boolean visible = Boolean.parseBoolean(tokens.get(10));
            int slots = Integer.parseInt(tokens.get(11));
            int accepted = Integer.parseInt(tokens.get(12));
            internships.put(id, new Internship(id, title, description, level, preferredMajor, opening, closing, status,
                    companyName, repId, visible, slots, accepted));
        }
        return internships;
    }

    public void saveInternships(Collection<Internship> internships) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(
                "InternshipID,Title,Description,Level,PreferredMajor,OpeningDate,ClosingDate,Status,CompanyName,RepresentativeId,Visible,Slots,AcceptedCount");
        for (Internship internship : internships) {
            lines.add(CsvUtils.toCsv(List.of(
                    internship.getId(),
                    internship.getTitle(),
                    Optional.ofNullable(internship.getDescription()).orElse(""),
                    internship.getLevel().name(),
                    Optional.ofNullable(internship.getPreferredMajor()).orElse(""),
                    internship.getOpeningDate().format(DATE_FORMAT),
                    internship.getClosingDate().format(DATE_FORMAT),
                    internship.getStatus().name(),
                    internship.getCompanyName(),
                    internship.getRepresentativeId(),
                    Boolean.toString(internship.isVisible()),
                    Integer.toString(internship.getSlots()),
                    Integer.toString(internship.getAcceptedCount()))));
        }
        Files.write(internshipsFile, lines, StandardCharsets.UTF_8);
    }

    public Map<String, InternshipApplication> loadApplications() throws IOException {
        Map<String, InternshipApplication> applications = new HashMap<>();
        if (!Files.exists(applicationsFile)) {
            return applications;
        }
        List<String> lines = Files.readAllLines(applicationsFile, StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) {
            List<String> tokens = CsvUtils.parseLine(lines.get(i));
            if (tokens.size() < 6) {
                continue;
            }
            String id = tokens.get(0);
            String studentId = tokens.get(1);
            String internshipId = tokens.get(2);
            ApplicationStatus status = ApplicationStatus.fromString(tokens.get(3));
            boolean withdrawRequested = Boolean.parseBoolean(tokens.get(4));
            boolean studentAccepted = Boolean.parseBoolean(tokens.get(5));
            applications.put(id,
                    new InternshipApplication(id, studentId, internshipId, status, withdrawRequested, studentAccepted));
        }
        return applications;
    }

    public void saveApplications(Collection<InternshipApplication> applications) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("ApplicationID,StudentID,InternshipID,Status,WithdrawRequested,StudentAccepted");
        for (InternshipApplication application : applications) {
            lines.add(CsvUtils.toCsv(List.of(
                    application.getId(),
                    application.getStudentId(),
                    application.getInternshipId(),
                    application.getStatus().name(),
                    Boolean.toString(application.isWithdrawRequested()),
                    Boolean.toString(application.isStudentAccepted()))));
        }
        Files.write(applicationsFile, lines, StandardCharsets.UTF_8);
    }

    public String nextInternshipId(Map<String, Internship> internships) {
        return "INT-" + UUID.randomUUID();
    }

    public String nextApplicationId() {
        return "APP-" + UUID.randomUUID();
    }

    public Path getDataDir() {
        return dataDir;
    }

    public Path getStudentsFile() {
        return studentsFile;
    }

    public Path getStaffFile() {
        return staffFile;
    }

    public Path getRepsFile() {
        return repsFile;
    }

    public Path getInternshipsFile() {
        return internshipsFile;
    }

    public Path getApplicationsFile() {
        return applicationsFile;
    }
}
