package com.smartwater.billing.resident.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.smartwater.billing.common.entity.User;
import com.smartwater.billing.owner.entity.Apartment;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "household_size_history", indexes = {
    @Index(name = "idx_hh_hist_apt_from", columnList = "apartment_id, effective_from"),
    @Index(name = "idx_hh_hist_occupancy", columnList = "occupancy_id")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class HouseholdSizeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "occupancy_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ApartmentOccupancy occupancy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Apartment apartment;

    @Column(name = "household_size", nullable = false)
    private Integer householdSize;

    @Column(name = "children_count")
    private Integer childrenCount;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User changedByUser;

    @Column(name = "changed_by_role", nullable = false, length = 50)
    private String changedByRole;

    @Column(name = "reason", nullable = false, length = 50)
    private String reason; // MOVE_IN, BIRTH, FAMILY_JOINED, FAMILY_LEFT, GUESTS_STAYING, CORRECTION, OTHER

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "sanity_warning")
    private Boolean sanityWarning = false;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "ACTIVE"; // ACTIVE, PENDING_APPROVAL, REJECTED, SUPERSEDED

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public HouseholdSizeHistory() {}

    public HouseholdSizeHistory(Long id, ApartmentOccupancy occupancy, Apartment apartment, Integer householdSize,
                                Integer childrenCount, LocalDate effectiveFrom, LocalDate effectiveTo,
                                User changedByUser, String changedByRole, String reason, String note,
                                Boolean sanityWarning, String status, LocalDateTime createdAt) {
        this.id = id;
        this.occupancy = occupancy;
        this.apartment = apartment;
        this.householdSize = householdSize;
        this.childrenCount = childrenCount;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.changedByUser = changedByUser;
        this.changedByRole = changedByRole;
        this.reason = reason;
        this.note = note;
        this.sanityWarning = sanityWarning;
        this.status = status;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "ACTIVE";
        }
        if (sanityWarning == null) {
            sanityWarning = false;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ApartmentOccupancy getOccupancy() { return occupancy; }
    public void setOccupancy(ApartmentOccupancy occupancy) { this.occupancy = occupancy; }

    public Apartment getApartment() { return apartment; }
    public void setApartment(Apartment apartment) { this.apartment = apartment; }

    public Integer getHouseholdSize() { return householdSize; }
    public void setHouseholdSize(Integer householdSize) { this.householdSize = householdSize; }

    public Integer getChildrenCount() { return childrenCount; }
    public void setChildrenCount(Integer childrenCount) { this.childrenCount = childrenCount; }

    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }

    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }

    public User getChangedByUser() { return changedByUser; }
    public void setChangedByUser(User changedByUser) { this.changedByUser = changedByUser; }

    public String getChangedByRole() { return changedByRole; }
    public void setChangedByRole(String changedByRole) { this.changedByRole = changedByRole; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Boolean getSanityWarning() { return sanityWarning; }
    public void setSanityWarning(Boolean sanityWarning) { this.sanityWarning = sanityWarning; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private ApartmentOccupancy occupancy;
        private Apartment apartment;
        private Integer householdSize;
        private Integer childrenCount;
        private LocalDate effectiveFrom;
        private LocalDate effectiveTo;
        private User changedByUser;
        private String changedByRole;
        private String reason;
        private String note;
        private Boolean sanityWarning = false;
        private String status = "ACTIVE";
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder occupancy(ApartmentOccupancy occupancy) { this.occupancy = occupancy; return this; }
        public Builder apartment(Apartment apartment) { this.apartment = apartment; return this; }
        public Builder householdSize(Integer householdSize) { this.householdSize = householdSize; return this; }
        public Builder childrenCount(Integer childrenCount) { this.childrenCount = childrenCount; return this; }
        public Builder effectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; return this; }
        public Builder effectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; return this; }
        public Builder changedByUser(User changedByUser) { this.changedByUser = changedByUser; return this; }
        public Builder changedByRole(String changedByRole) { this.changedByRole = changedByRole; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }
        public Builder note(String note) { this.note = note; return this; }
        public Builder sanityWarning(Boolean sanityWarning) { this.sanityWarning = sanityWarning; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public HouseholdSizeHistory build() {
            return new HouseholdSizeHistory(id, occupancy, apartment, householdSize, childrenCount, effectiveFrom, effectiveTo, changedByUser, changedByRole, reason, note, sanityWarning, status, createdAt);
        }
    }
}
