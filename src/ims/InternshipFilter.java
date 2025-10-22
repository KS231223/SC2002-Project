package ims;

import java.time.LocalDate;
import java.util.Optional;

public class InternshipFilter {
    private InternshipStatus status;
    private String preferredMajor;
    private InternshipLevel level;
    private LocalDate closingDateBefore;

    public Optional<InternshipStatus> getStatus() {
        return Optional.ofNullable(status);
    }

    public void setStatus(InternshipStatus status) {
        this.status = status;
    }

    public Optional<String> getPreferredMajor() {
        return Optional.ofNullable(preferredMajor);
    }

    public void setPreferredMajor(String preferredMajor) {
        this.preferredMajor = (preferredMajor == null || preferredMajor.isBlank()) ? null : preferredMajor.trim();
    }

    public Optional<InternshipLevel> getLevel() {
        return Optional.ofNullable(level);
    }

    public void setLevel(InternshipLevel level) {
        this.level = level;
    }

    public Optional<LocalDate> getClosingDateBefore() {
        return Optional.ofNullable(closingDateBefore);
    }

    public void setClosingDateBefore(LocalDate closingDateBefore) {
        this.closingDateBefore = closingDateBefore;
    }

    public void clear() {
        this.status = null;
        this.preferredMajor = null;
        this.level = null;
        this.closingDateBefore = null;
    }
}
