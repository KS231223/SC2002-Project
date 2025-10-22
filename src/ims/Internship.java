package ims;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Internship {
    private final String id;
    private String title;
    private String description;
    private InternshipLevel level;
    private String preferredMajor;
    private LocalDate openingDate;
    private LocalDate closingDate;
    private InternshipStatus status;
    private final String companyName;
    private final String representativeId;
    private boolean visible;
    private int slots;
    private int acceptedCount;
    private final List<String> applicationIds = new ArrayList<>();

    public Internship(
            String id,
            String title,
            String description,
            InternshipLevel level,
            String preferredMajor,
            LocalDate openingDate,
            LocalDate closingDate,
            InternshipStatus status,
            String companyName,
            String representativeId,
            boolean visible,
            int slots,
            int acceptedCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.level = level;
        this.preferredMajor = preferredMajor;
        this.openingDate = openingDate;
        this.closingDate = closingDate;
        this.status = status;
        this.companyName = companyName;
        this.representativeId = representativeId;
        this.visible = visible;
        this.slots = slots;
        this.acceptedCount = acceptedCount;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public InternshipLevel getLevel() {
        return level;
    }

    public void setLevel(InternshipLevel level) {
        this.level = level;
    }

    public String getPreferredMajor() {
        return preferredMajor;
    }

    public void setPreferredMajor(String preferredMajor) {
        this.preferredMajor = preferredMajor;
    }

    public LocalDate getOpeningDate() {
        return openingDate;
    }

    public void setOpeningDate(LocalDate openingDate) {
        this.openingDate = openingDate;
    }

    public LocalDate getClosingDate() {
        return closingDate;
    }

    public void setClosingDate(LocalDate closingDate) {
        this.closingDate = closingDate;
    }

    public InternshipStatus getStatus() {
        return status;
    }

    public void setStatus(InternshipStatus status) {
        this.status = status;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getRepresentativeId() {
        return representativeId;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getSlots() {
        return slots;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }

    public int getAcceptedCount() {
        return acceptedCount;
    }

    public void setAcceptedCount(int acceptedCount) {
        this.acceptedCount = acceptedCount;
    }

    public List<String> getApplicationIds() {
        return applicationIds;
    }

    public boolean hasCapacity() {
        return acceptedCount < slots;
    }
}
