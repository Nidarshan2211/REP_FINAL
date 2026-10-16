package com.smartwater.billing.resident.entity;

import com.smartwater.billing.owner.entity.Apartment;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "apartment_occupancies", indexes = {
    @Index(name = "idx_occupancy_apartment", columnList = "apartment_id"),
    @Index(name = "idx_occupancy_resident", columnList = "resident_id"),
    @Index(name = "idx_occupancy_status", columnList = "status")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ApartmentOccupancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "occupancy_id")
    private Long occupancyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Apartment apartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user"})
    private Resident resident;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OccupancyStatus status;

    @Column(name = "move_out_reason", length = 255)
    private String moveOutReason;

    // Household Size Capture
    @Column(name = "household_size")
    private Integer householdSize;

    @Column(name = "children_count")
    private Integer childrenCount;

    @Column(name = "household_size_source", length = 30)
    private String householdSizeSource;

    @Column(name = "household_size_confirmed", nullable = false)
    private Boolean householdSizeConfirmed = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public ApartmentOccupancy() {}

    public ApartmentOccupancy(Long occupancyId, Apartment apartment, Resident resident, LocalDateTime startDate,
                              LocalDateTime endDate, OccupancyStatus status, String moveOutReason,
                              Integer householdSize, Integer childrenCount, String householdSizeSource,
                              Boolean householdSizeConfirmed, LocalDateTime createdAt) {
        this.occupancyId = occupancyId;
        this.apartment = apartment;
        this.resident = resident;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.moveOutReason = moveOutReason;
        this.householdSize = householdSize;
        this.childrenCount = childrenCount;
        this.householdSizeSource = householdSizeSource;
        this.householdSizeConfirmed = householdSizeConfirmed != null ? householdSizeConfirmed : false;
        this.createdAt = createdAt;
    }

    // Legacy constructor compatibility
    public ApartmentOccupancy(Long occupancyId, Apartment apartment, Resident resident, LocalDateTime startDate,
                              LocalDateTime endDate, OccupancyStatus status, String moveOutReason, LocalDateTime createdAt) {
        this(occupancyId, apartment, resident, startDate, endDate, status, moveOutReason, null, null, null, false, createdAt);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (startDate == null) {
            startDate = LocalDateTime.now();
        }
        if (status == null) {
            status = OccupancyStatus.ACTIVE;
        }
        if (householdSizeConfirmed == null) {
            householdSizeConfirmed = false;
        }
    }

    public Long getOccupancyId() { return occupancyId; }
    public void setOccupancyId(Long occupancyId) { this.occupancyId = occupancyId; }

    public Apartment getApartment() { return apartment; }
    public void setApartment(Apartment apartment) { this.apartment = apartment; }

    public Resident getResident() { return resident; }
    public void setResident(Resident resident) { this.resident = resident; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public OccupancyStatus getStatus() { return status; }
    public void setStatus(OccupancyStatus status) { this.status = status; }

    public String getMoveOutReason() { return moveOutReason; }
    public void setMoveOutReason(String moveOutReason) { this.moveOutReason = moveOutReason; }

    public Integer getHouseholdSize() { return householdSize; }
    public void setHouseholdSize(Integer householdSize) { this.householdSize = householdSize; }

    public Integer getChildrenCount() { return childrenCount; }
    public void setChildrenCount(Integer childrenCount) { this.childrenCount = childrenCount; }

    public String getHouseholdSizeSource() { return householdSizeSource; }
    public void setHouseholdSizeSource(String householdSizeSource) { this.householdSizeSource = householdSizeSource; }

    public Boolean getHouseholdSizeConfirmed() { return householdSizeConfirmed; }
    public void setHouseholdSizeConfirmed(Boolean householdSizeConfirmed) { this.householdSizeConfirmed = householdSizeConfirmed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long occupancyId;
        private Apartment apartment;
        private Resident resident;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private OccupancyStatus status = OccupancyStatus.ACTIVE;
        private String moveOutReason;
        private Integer householdSize;
        private Integer childrenCount;
        private String householdSizeSource;
        private Boolean householdSizeConfirmed = false;
        private LocalDateTime createdAt;

        public Builder occupancyId(Long occupancyId) { this.occupancyId = occupancyId; return this; }
        public Builder apartment(Apartment apartment) { this.apartment = apartment; return this; }
        public Builder resident(Resident resident) { this.resident = resident; return this; }
        public Builder startDate(LocalDateTime startDate) { this.startDate = startDate; return this; }
        public Builder endDate(LocalDateTime endDate) { this.endDate = endDate; return this; }
        public Builder status(OccupancyStatus status) { this.status = status; return this; }
        public Builder moveOutReason(String moveOutReason) { this.moveOutReason = moveOutReason; return this; }
        public Builder householdSize(Integer householdSize) { this.householdSize = householdSize; return this; }
        public Builder childrenCount(Integer childrenCount) { this.childrenCount = childrenCount; return this; }
        public Builder householdSizeSource(String householdSizeSource) { this.householdSizeSource = householdSizeSource; return this; }
        public Builder householdSizeConfirmed(Boolean householdSizeConfirmed) { this.householdSizeConfirmed = householdSizeConfirmed; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public ApartmentOccupancy build() {
            return new ApartmentOccupancy(occupancyId, apartment, resident, startDate, endDate, status,
                    moveOutReason, householdSize, childrenCount, householdSizeSource, householdSizeConfirmed, createdAt);
        }
    }

    public enum OccupancyStatus {
        ACTIVE,
        ENDED,
        PENDING_APPROVAL,
        REJECTED,
        CANCELLED
    }
}
