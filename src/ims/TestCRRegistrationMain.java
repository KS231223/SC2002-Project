package ims;

import common.DatabaseManager;
import common.EntityStore;
import common.Router;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Replays CLI interactions that create pending company representative registrations.
 * Choose a scenario key to focus on specific coverage (for example: {@code single}).
 */
public final class TestCRRegistrationMain {

    private TestCRRegistrationMain() {
        // Utility class
    }

    private static final Path PENDING_CR_FILE = Paths.get("resources", "pending_cr.csv");
    private static final String CSV_HEADER =
            "CRID,Password,Name,CompanyName,Department,Position,Email,PreferredMajors,PreferredInternshipLevel,PreferredClosingDate";

    private record Scenario(String name, String[] steps) {}

    private record CrProfile(String email, String name, String company, String department, String position, String password) {}

    private static final CrProfile LUMINA_PARTNERS = new CrProfile(
            "luna.alvarez@luminapartners.com",
            "Luna Alvarez",
            "Lumina Partners",
            "Strategic Partnerships",
            "Director",
            "Lumina#2024"
    );

    private static final CrProfile CIRRUS_ALLIANCE = new CrProfile(
            "kai.tan@cirrusalliance.io",
            "Kai Tan",
            "Cirrus Alliance",
            "People Operations",
            "Talent Lead",
            "Cirrus@123"
    );

    private static final Scenario DEFAULT_SCENARIO = dualRegistrationScenario();
    private static final Scenario SINGLE_SCENARIO = singleRegistrationScenario();
    private static final Map<String, Scenario> SCENARIO_REGISTRY = Map.ofEntries(
            Map.entry("cr-batch", DEFAULT_SCENARIO),
            Map.entry("batch", DEFAULT_SCENARIO),
            Map.entry("dual", DEFAULT_SCENARIO),
            Map.entry("default", DEFAULT_SCENARIO),
            Map.entry("single", SINGLE_SCENARIO),
            Map.entry("cr-single", SINGLE_SCENARIO)
    );

    /**
     * Entry point that seeds baseline data and replays the selected registration scenario.
     *
     * @param args optional scenario key (defaults to the batch run)
     */
    public static void main(String[] args) {
        Scenario scenario = resolveScenario(args);
        System.out.println("\n>>> Selected scenario: " + scenario.name());
        baselineData();
        runScenario(scenario);
    }

    private static Scenario resolveScenario(String[] args) {
        if (args == null || args.length == 0) {
            return DEFAULT_SCENARIO;
        }
        String key = args[0].toLowerCase(Locale.ROOT);
        Scenario scenario = SCENARIO_REGISTRY.get(key);
        if (scenario == null) {
            System.out.println("Unknown scenario '" + key + "'. Falling back to batch registration.");
            return DEFAULT_SCENARIO;
        }
        return scenario;
    }

    private static Scenario dualRegistrationScenario() {
        ScriptBuilder script = new ScriptBuilder()
                .register(LUMINA_PARTNERS)
                .register(CIRRUS_ALLIANCE)
                .exitAuthentication();
        return new Scenario("Registers two CR accounts for Lumina Partners and Cirrus Alliance", script.build());
    }

    private static Scenario singleRegistrationScenario() {
        ScriptBuilder script = new ScriptBuilder()
                .register(LUMINA_PARTNERS)
                .exitAuthentication();
        return new Scenario("Registers a single CR account for Lumina Partners", script.build());
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    private static void runScenario(Scenario scenario) {
        System.out.println("Running scenario: " + scenario.name());
        String joinedScript = String.join(System.lineSeparator(), scenario.steps()) + System.lineSeparator();
        ByteArrayInputStream input = new ByteArrayInputStream(joinedScript.getBytes(StandardCharsets.UTF_8));
        Scanner scanner = new Scanner(input);
        Router router = new Router();
        EntityStore entityStore = new DatabaseManager();

        try {
            Authentication authentication = new Authentication(router, scanner, entityStore);
            authentication.start();
        } catch (RuntimeException ex) {
            System.err.println("Scenario terminated with exception: " + ex.getMessage());
            ex.printStackTrace(System.err);
        } finally {
            scanner.close();
        }
    }

    private static void baselineData() {
        ensurePendingFile();
        pruneExistingEntries(LUMINA_PARTNERS, CIRRUS_ALLIANCE);
    }

    private static void ensurePendingFile() {
        try {
            Files.createDirectories(PENDING_CR_FILE.getParent());
            if (!Files.exists(PENDING_CR_FILE)) {
                Files.writeString(PENDING_CR_FILE, CSV_HEADER + System.lineSeparator(), StandardCharsets.UTF_8);
                return;
            }
            List<String> lines = Files.readAllLines(PENDING_CR_FILE);
            if (lines.isEmpty() || !isHeaderLine(lines.get(0))) {
                lines.add(0, CSV_HEADER);
                Files.write(PENDING_CR_FILE, lines, StandardCharsets.UTF_8);
            }
        } catch (IOException ex) {
            System.err.println("Unable to prepare pending CR file: " + ex.getMessage());
        }
    }

    private static void pruneExistingEntries(CrProfile... profiles) {
        if (profiles == null || profiles.length == 0) {
            return;
        }
        Set<String> targetIds = new HashSet<>();
        for (CrProfile profile : profiles) {
            if (profile != null) {
                targetIds.add(profile.email());
            }
        }
        if (targetIds.isEmpty()) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(PENDING_CR_FILE);
            List<String> updated = new ArrayList<>();
            boolean headerRetained = false;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (i == 0 && isHeaderLine(line)) {
                    updated.add(line);
                    headerRetained = true;
                    continue;
                }
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (parts.length == 0) {
                    continue;
                }
                if (targetIds.contains(parts[0])) {
                    continue;
                }
                updated.add(line);
            }
            if (!headerRetained) {
                updated.add(0, CSV_HEADER);
            }
            Files.write(PENDING_CR_FILE, updated, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            System.err.println("Unable to prune pending CR entries: " + ex.getMessage());
        }
    }

    private static boolean isHeaderLine(String line) {
        if (line == null) {
            return false;
        }
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return true;
        }
        return trimmed.toUpperCase(Locale.ROOT).startsWith("CRID");
    }

    private static final class ScriptBuilder {
        private final ArrayList<String> tokens = new ArrayList<>();

        ScriptBuilder register(CrProfile profile) {
            tokens.add("2");
            tokens.add(profile.email());
            tokens.add(profile.name());
            tokens.add(profile.company());
            tokens.add(profile.department());
            tokens.add(profile.position());
            tokens.add(profile.password());
            return this;
        }

        ScriptBuilder exitAuthentication() {
            tokens.add("3");
            return this;
        }

        String[] build() {
            return tokens.toArray(String[]::new);
        }
    }
}
