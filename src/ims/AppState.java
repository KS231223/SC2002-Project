package ims;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class AppState {
    private final DataStore dataStore;
    private Map<String, Student> students = new HashMap<>();
    private Map<String, CareerCenterStaff> staff = new HashMap<>();
    private Map<String, CompanyRepresentative> companyReps = new HashMap<>();
    private Map<String, Internship> internships = new HashMap<>();
    private Map<String, InternshipApplication> applications = new HashMap<>();

    public AppState() {
        this.dataStore = new DataStore(Paths.get("."));
    }

    public void load() throws IOException {
        dataStore.initialize();
        students = dataStore.loadStudents();
        staff = dataStore.loadStaff();
        companyReps = dataStore.loadCompanyReps();
        internships = dataStore.loadInternships();
        applications = dataStore.loadApplications();
    }

    public void persistAll() throws IOException {
        dataStore.saveStudents(students.values());
        dataStore.saveStaff(staff.values());
        dataStore.saveCompanyReps(companyReps.values());
        dataStore.saveInternships(internships.values());
        dataStore.saveApplications(applications.values());
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public Map<String, Student> getStudents() {
        return students;
    }

    public Map<String, CareerCenterStaff> getStaff() {
        return staff;
    }

    public Map<String, CompanyRepresentative> getCompanyReps() {
        return companyReps;
    }

    public Map<String, Internship> getInternships() {
        return internships;
    }

    public Map<String, InternshipApplication> getApplications() {
        return applications;
    }
}
