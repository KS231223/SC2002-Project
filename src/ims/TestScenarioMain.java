package ims;

import common.Router;
import common.StudentEntity;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;

/**
 * Exercises student flows by replaying canned input through the existing CLI controllers.
 * Run without arguments for the full Student A walk-through, or provide one of the scenario
 * keys to focus on a specific flow (for example: {@code accept-offer}).
 */
public final class TestScenarioMain {

    private TestScenarioMain() {
        // Utility class
    }

    private static final String DEFAULT_PASSWORD = "password";
    private static final String STUDENT_A = "U2310001A";
    private static final String STUDENT_B = "U2310002B";
    private static final String INTERNSHIP_ID = "b93b8f0c-c09d-4939-b59c-4607f506a265";
    private static final String APPLICATION_WITHDRAW_ID = "94ba951c-3b76-4edf-b5fc-de79ad88ccd2";
    private static final String APPLICATION_APPROVED_ID = "f7bcd5d5-660a-470f-a64f-3b6c0ae14f8f";

    private static final Path USERS_FILE = Paths.get("resources", "users.csv");
    private static final Path STUDENTS_FILE = Paths.get("resources", "student.csv");
    private static final Path APPLICATIONS_FILE = Paths.get("resources", "internship_applications.csv");

    /** Lightweight value object describing a named scenario and its scripted steps. */
    private record Scenario(String name, String[] steps) {}

    /**
     * Entry point that resolves the requested scenario, seeds baseline data, and replays the flow.
     *
     * @param args optional scenario key (defaults to the full student flow)
     */
    public static void main(String[] args) {
        Scenario scenario = resolveScenario(args);
        System.out.println("\n>>> Selected scenario: " + scenario.name());
        baselineData();
        runScenario(scenario);
    }

    /**
     * Maps command-line arguments to a known scenario configuration.
     *
     * @param args command-line arguments (may be {@code null})
     * @return resolved scenario definition
     */
    private static Scenario resolveScenario(String[] args) {
        if (args == null || args.length == 0) {
            return studentFullMenuScenario();
        }

        String key = args[0].toLowerCase(Locale.ROOT);
        return switch (key) {
            case "student-full", "student-a" -> studentFullMenuScenario();
            case "accept-offer", "student-b" -> studentAcceptScenario();
            default -> {
                System.out.println("Unknown scenario '" + key + "'. Falling back to full student flow.");
                yield studentFullMenuScenario();
            }
        };
    }

    /**
     * Builds the full student flow scenario comprising filtering, applying, and withdrawing.
     *
     * @return scenario containing menu inputs and actions
     */
    private static Scenario studentFullMenuScenario() {
        String[] steps = {
            "1", STUDENT_A, DEFAULT_PASSWORD,
            "1", "",                                          // view internships then return
            "2",                                               // enter filter editor
            "1", "2",                                         // level -> Basic
            "2", "0",                                         // company -> None
            "3", "3",                                         // status -> Pending
            "4", "CSC",                                       // preferred major -> CSC
            "5", "2",                                         // closing sort -> Soonest
            "6",                                               // save filters
            "3",                                               // clear filters from home page
            "4", INTERNSHIP_ID,                                 // apply for internship
            "5", "",                                          // view applications
            "6", APPLICATION_WITHDRAW_ID,                       // submit withdrawal request
            "8", "TempPass123!",                               // change password to temp
            "8", DEFAULT_PASSWORD,                              // reset password
            "9",                                               // logout
            "3"                                                // exit authentication menu
        };
        return new Scenario("Student A covers main menu", steps);
    }

    /**
     * Builds the focused scenario where a student accepts an existing offer.
     *
     * @return scenario tailored to the accept-offer flow
     */
    private static Scenario studentAcceptScenario() {
        String[] steps = {
            "1", STUDENT_B, DEFAULT_PASSWORD,
            "7", APPLICATION_APPROVED_ID,
            "9",
            "3"
        };
        return new Scenario("Student B accepts an offer", steps);
    }

    /**
     * Executes the selected scenario by streaming scripted input through the CLI.
     *
     * @param scenario scenario descriptor containing metadata and steps
     */
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    private static void runScenario(Scenario scenario) {
        System.out.println("Running scenario: " + scenario.name());
        String joinedScript = String.join(System.lineSeparator(), Arrays.asList(scenario.steps())) + System.lineSeparator();
        ByteArrayInputStream input = new ByteArrayInputStream(joinedScript.getBytes(StandardCharsets.UTF_8));
        Scanner scanner = new Scanner(input);
        Router router = new Router();

        try {
            Authentication authentication = new Authentication(router, scanner);
            authentication.start();
        } catch (RuntimeException ex) {
            System.err.println("Scenario terminated with exception: " + ex.getMessage());
            ex.printStackTrace(System.err);
        } finally {
            scanner.close();
        }
    }

    /**
     * Ensures user, student, and application records are aligned with scenario expectations.
     */
    private static void baselineData() {
        ensureUserPassword(STUDENT_A, DEFAULT_PASSWORD);
        ensureUserPassword(STUDENT_B, DEFAULT_PASSWORD);
        ensureStudentAcceptedValue(STUDENT_A, StudentEntity.NO_FILTER_VALUE);
        ensureStudentAcceptedValue(STUDENT_B, StudentEntity.NO_FILTER_VALUE);
        ensureApplicationStatus(APPLICATION_APPROVED_ID, "Approved");
    }

    /**
     * Updates the users CSV so that the desired password is set for the specified user.
     *
     * @param userId          unique user identifier
     * @param desiredPassword password to enforce prior to running the scenario
     */
    private static void ensureUserPassword(String userId, String desiredPassword) {
        try {
            var lines = Files.readAllLines(USERS_FILE);
            boolean updated = false;
            for (int i = 0; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",", -1);
                if (parts.length < 2) {
                    continue;
                }
                if (parts[0].equals(userId) && !desiredPassword.equals(parts[1])) {
                    parts[1] = desiredPassword;
                    lines.set(i, String.join(",", parts));
                    updated = true;
                    break;
                }
            }
            if (updated) {
                Files.write(USERS_FILE, lines);
            }
        } catch (IOException ex) {
            System.err.println("Unable to reset password for " + userId + ": " + ex.getMessage());
        }
    }

    /**
     * Resets the accepted internship field for the supplied student to ensure a clean slate.
     *
     * @param studentId     student identifier
     * @param acceptedValue value to apply to the accepted internship column
     */
    private static void ensureStudentAcceptedValue(String studentId, String acceptedValue) {
        try {
            var lines = Files.readAllLines(STUDENTS_FILE);
            boolean updated = false;
            int fieldIndex = StudentEntity.StudentField.AcceptedInternshipID.ordinal();
            for (int i = 0; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",", -1);
                if (parts.length <= fieldIndex) {
                    continue;
                }
                if (parts[0].equals(studentId) && !acceptedValue.equals(parts[fieldIndex])) {
                    parts[fieldIndex] = acceptedValue;
                    lines.set(i, String.join(",", parts));
                    updated = true;
                    break;
                }
            }
            if (updated) {
                Files.write(STUDENTS_FILE, lines);
            }
        } catch (IOException ex) {
            System.err.println("Unable to reset accepted internship for " + studentId + ": " + ex.getMessage());
        }
    }

    /**
     * Guarantees that the application with the provided identifier has the desired status.
     *
     * @param applicationId application identifier to update
     * @param status        target status value
     */
    private static void ensureApplicationStatus(String applicationId, String status) {
        try {
            var lines = Files.readAllLines(APPLICATIONS_FILE);
            boolean updated = false;
            for (int i = 0; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",", -1);
                if (parts.length < 4) {
                    continue;
                }
                if (parts[0].equals(applicationId) && !status.equals(parts[3])) {
                    parts[3] = status;
                    lines.set(i, String.join(",", parts));
                    updated = true;
                    break;
                }
            }
            if (updated) {
                Files.write(APPLICATIONS_FILE, lines);
            }
        } catch (IOException ex) {
            System.err.println("Unable to reset application status for " + applicationId + ": " + ex.getMessage());
        }
    }
}
