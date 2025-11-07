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
        Path candidate = findWithProjectStructure(cwd);
        if (candidate != null) {
            return candidate;
        }
        Path namedChild = cwd.resolve("SC2002-Project");
        candidate = findWithProjectStructure(namedChild);
        if (candidate != null) {
            return candidate;
        }
        Path current = cwd;
        while ((current = current.getParent()) != null) {
            candidate = findWithProjectStructure(current);
            if (candidate != null) {
                return candidate;
            }
        }
        return cwd;
    }

    private static Path findWithProjectStructure(Path dir) {
        if (dir == null) {
            return null;
        }
        Path resourcesDir = dir.resolve("resources");
        Path srcDir = dir.resolve("src");
        if (Files.isDirectory(resourcesDir) && Files.isDirectory(srcDir)) {
            return dir;
        }
        return null;
    }
}
