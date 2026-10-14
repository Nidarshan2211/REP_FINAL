package com.smartwater.billing.resident.entity;

import com.smartwater.billing.owner.entity.Apartment;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "occupancy_requests", indexes = {
    @Index(name = "idx_req_apartment", columnList = "apartment_id"),
    @Index(name = "idx_req_resident", columnList = "resident_id"),
    @Index(name = "idx_req_status", columnList = "status")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OccupancyRequest {

    public enum RequestStatus {
        PENDING,
        APPROVED,
        REJECTED,
        CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Apartment apartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user"})
    private Resident resident;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "reviewed_by", length = 150)
    private String reviewedBy;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    @Column(name = "notes", length = 255)
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public OccupancyRequest() {}

    public OccupancyRequest(Long requestId, Apartment apartment, Resident resident, RequestStatus status, LocalDateTime requestDate, LocalDateTime reviewedAt, String reviewedBy, String rejectionReason, String notes, LocalDateTime createdAt) {
        this.requestId = requestId;
        this.apartment = apartment;
        this.resident = resident;
        this.status = status;
        this.requestDate = requestDate;
        this.reviewedAt = reviewedAt;
        this.reviewedBy = reviewedBy;
        this.rejectionReason = rejectionReason;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (requestDate == null) {
            requestDate = LocalDateTime.now();
        }
        if (status == null) {
            status = RequestStatus.PENDING;
        }
    }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public Apartment getApartment() { return apartment; }
    public void setApartment(Apartment apartment) { this.apartment = apartment; }

    public Resident getResident() { return resident; }
    public void setResident(Resident resident) { this.resident = resident; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long requestId;
        private Apartment apartment;
        private Resident resident;
        private RequestStatus status = RequestStatus.PENDING;
        private LocalDateTime requestDate;
        private LocalDateTime reviewedAt;
        private String reviewedBy;
        private String rejectionReason;
        private String notes;
        private LocalDateTime createdAt;

        public Builder requestId(Long requestId) { this.requestId = requestId; return this; }
        public Builder apartment(Apartment apartment) { this.apartment = apartment; return this; }
        public Builder resident(Resident resident) { this.resident = resident; return this; }
        public Builder status(RequestStatus status) { this.status = status; return this; }
        public Builder requestDate(LocalDateTime requestDate) { this.requestDate = requestDate; return this; }
        public Builder reviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; return this; }
        public Builder reviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; return this; }
        public Builder rejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public OccupancyRequest build() {
            return new OccupancyRequest(requestId, apartment, resident, status, requestDate, reviewedAt, reviewedBy, rejectionReason, notes, createdAt);
        }
    }
}
