package ims;

public enum InternshipLevel {
    BASIC,
    INTERMEDIATE,
    ADVANCED;

    public static InternshipLevel fromString(String value) {
        for (InternshipLevel level : values()) {
            if (level.name().equalsIgnoreCase(value.trim())) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown level: " + value);
    }
}
