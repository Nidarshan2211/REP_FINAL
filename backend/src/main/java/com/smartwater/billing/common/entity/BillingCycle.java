package com.smartwater.billing.common.entity;


import com.smartwater.billing.owner.entity.Building;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "billing_cycles", uniqueConstraints = {
    @UniqueConstraint(name = "uk_building_billing_month", columnNames = {"building_id", "billing_month"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BillingCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "building_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "owner"})
    private Building building;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "billing_month", nullable = false)
    private LocalDate billingMonth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CycleStatus status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public BillingCycle() {}

    public BillingCycle(Long id, Building building, LocalDate startDate, LocalDate endDate, LocalDate billingMonth, CycleStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.building = building;
        this.startDate = startDate;
        this.endDate = endDate;
        this.billingMonth = billingMonth;
        this.status = status;
        this.createdAt = createdAt;
    }

    @PrePersist
    @PreUpdate
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = CycleStatus.OPEN;
        if (startDate != null && billingMonth == null) {
            billingMonth = startDate.withDayOfMonth(1);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Building getBuilding() { return building; }
    public void setBuilding(Building building) { this.building = building; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { 
        this.startDate = startDate; 
        if (startDate != null) {
            this.billingMonth = startDate.withDayOfMonth(1);
        }
    }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDate getBillingMonth() { return billingMonth; }
    public void setBillingMonth(LocalDate billingMonth) { this.billingMonth = billingMonth; }

    public CycleStatus getStatus() { return status; }
    public void setStatus(CycleStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Building building;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDate billingMonth;
        private CycleStatus status;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder building(Building building) { this.building = building; return this; }
        public Builder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public Builder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public Builder billingMonth(LocalDate billingMonth) { this.billingMonth = billingMonth; return this; }
        public Builder status(CycleStatus status) { this.status = status; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public BillingCycle build() {
            LocalDate month = billingMonth;
            if (month == null && startDate != null) {
                month = startDate.withDayOfMonth(1);
            }
            return new BillingCycle(id, building, startDate, endDate, month, status, createdAt);
        }
    }

    public enum CycleStatus {
        OPEN,
        CLOSED,
        FINALIZED,
        ARCHIVED
    }
}
