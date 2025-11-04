package common;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Utility to resolve resource file paths regardless of working directory. */
public final class PathResolver {

    private static final Path PROJECT_ROOT = locateProjectRoot();

    private PathResolver() {
        // Utility class
    }

    public static String resource(String... segments) {
        Path base = PROJECT_ROOT.resolve("resources");
        for (String segment : segments) {
            base = base.resolve(segment);
        }
        return base.toString();
    }

    private static Path locateProjectRoot() {
        Path cwd = Paths.get("").toAbsolutePath();
        if (Files.isDirectory(cwd.resolve("resources"))) {
            return cwd;
        }
        Path projectDir = cwd.resolve("SC2002-Project");
        if (Files.isDirectory(projectDir.resolve("resources"))) {
            return projectDir;
        }
        return cwd;
    }
}
