package ims;

public enum InternshipStatus {
    PENDING,
    APPROVED,
    REJECTED,
    FILLED;

    public static InternshipStatus fromString(String value) {
        for (InternshipStatus status : values()) {
            if (status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown internship status: " + value);
    }
}
