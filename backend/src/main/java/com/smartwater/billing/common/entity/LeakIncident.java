package com.smartwater.billing.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.smartwater.billing.owner.entity.Apartment;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leak_incidents", indexes = {
    @Index(name = "idx_incident_apartment", columnList = "apartment_id"),
    @Index(name = "idx_incident_status", columnList = "status")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LeakIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Apartment apartment;

    @Column(name = "first_usage_id", nullable = false)
    private Long firstUsageId;

    @Column(name = "last_usage_id", nullable = false)
    private Long lastUsageId;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "OPEN"; // OPEN, RESOLVED, DISMISSED

    @Column(name = "peak_risk_score", nullable = false)
    private Double peakRiskScore;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public LeakIncident() {}

    public LeakIncident(Long id, Apartment apartment, Long firstUsageId, Long lastUsageId, String status, Double peakRiskScore, LocalDateTime openedAt, LocalDateTime resolvedAt) {
        this.id = id;
        this.apartment = apartment;
        this.firstUsageId = firstUsageId;
        this.lastUsageId = lastUsageId;
        this.status = status;
        this.peakRiskScore = peakRiskScore;
        this.openedAt = openedAt;
        this.resolvedAt = resolvedAt;
    }

    @PrePersist
    protected void onCreate() {
        if (openedAt == null) {
            openedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "OPEN";
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Apartment getApartment() { return apartment; }
    public void setApartment(Apartment apartment) { this.apartment = apartment; }

    public Long getFirstUsageId() { return firstUsageId; }
    public void setFirstUsageId(Long firstUsageId) { this.firstUsageId = firstUsageId; }

    public Long getLastUsageId() { return lastUsageId; }
    public void setLastUsageId(Long lastUsageId) { this.lastUsageId = lastUsageId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getPeakRiskScore() { return peakRiskScore; }
    public void setPeakRiskScore(Double peakRiskScore) { this.peakRiskScore = peakRiskScore; }

    public LocalDateTime getOpenedAt() { return openedAt; }
    public void setOpenedAt(LocalDateTime openedAt) { this.openedAt = openedAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Apartment apartment;
        private Long firstUsageId;
        private Long lastUsageId;
        private String status = "OPEN";
        private Double peakRiskScore;
        private LocalDateTime openedAt;
        private LocalDateTime resolvedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder apartment(Apartment apartment) { this.apartment = apartment; return this; }
        public Builder firstUsageId(Long firstUsageId) { this.firstUsageId = firstUsageId; return this; }
        public Builder lastUsageId(Long lastUsageId) { this.lastUsageId = lastUsageId; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder peakRiskScore(Double peakRiskScore) { this.peakRiskScore = peakRiskScore; return this; }
        public Builder openedAt(LocalDateTime openedAt) { this.openedAt = openedAt; return this; }
        public Builder resolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; return this; }

        public LeakIncident build() {
            return new LeakIncident(id, apartment, firstUsageId, lastUsageId, status, peakRiskScore, openedAt, resolvedAt);
        }
    }
}
