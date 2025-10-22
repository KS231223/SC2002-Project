package ims;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class CsvUtils {
    private CsvUtils() {
    }

    public static List<String> parseLine(String line) {
        List<String> tokens = new ArrayList<>();
        if (line == null || line.isEmpty()) {
            return tokens;
        }
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        tokens.add(current.toString());
        return tokens;
    }

    public static String toCsv(List<String> items) {
        return items.stream().map(CsvUtils::escape).collect(Collectors.joining(","));
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
    boolean requiresQuotes = value.contains(",") || value.contains("\n") || value.contains("\r") || value.contains("\"");
    String escaped = value.replace("\"", "\"\"");
        if (requiresQuotes) {
            return '"' + escaped + '"';
        }
        return escaped;
    }
}
