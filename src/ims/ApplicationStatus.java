package ims;

public enum ApplicationStatus {
    PENDING,
    SUCCESSFUL,
    UNSUCCESSFUL,
    WITHDRAWN;

    public static ApplicationStatus fromString(String value) {
        for (ApplicationStatus status : values()) {
            if (status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown application status: " + value);
    }
}
