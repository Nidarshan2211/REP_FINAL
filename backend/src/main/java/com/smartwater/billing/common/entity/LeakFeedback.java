package com.smartwater.billing.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.smartwater.billing.owner.entity.Apartment;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leak_feedback", indexes = {
    @Index(name = "idx_feedback_usage", columnList = "usage_id"),
    @Index(name = "idx_feedback_apartment", columnList = "apartment_id"),
    @Index(name = "idx_feedback_user", columnList = "submitted_by_user_id")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LeakFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usage_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private WaterUsage usage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Apartment apartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User submittedByUser;

    @Column(name = "submitter_role", nullable = false, length = 50)
    private String submitterRole;

    @Column(name = "feedback", nullable = false, length = 50)
    private String feedback; // REAL_LEAK_CONFIRMED, FALSE_ALARM, PLUMBING_WORK, GUESTS_VISITING, OTHER

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public LeakFeedback() {}

    public LeakFeedback(Long id, WaterUsage usage, Apartment apartment, User submittedByUser, String submitterRole, String feedback, String note, LocalDateTime createdAt) {
        this.id = id;
        this.usage = usage;
        this.apartment = apartment;
        this.submittedByUser = submittedByUser;
        this.submitterRole = submitterRole;
        this.feedback = feedback;
        this.note = note;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WaterUsage getUsage() { return usage; }
    public void setUsage(WaterUsage usage) { this.usage = usage; }

    public Apartment getApartment() { return apartment; }
    public void setApartment(Apartment apartment) { this.apartment = apartment; }

    public User getSubmittedByUser() { return submittedByUser; }
    public void setSubmittedByUser(User submittedByUser) { this.submittedByUser = submittedByUser; }

    public String getSubmitterRole() { return submitterRole; }
    public void setSubmitterRole(String submitterRole) { this.submitterRole = submitterRole; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private WaterUsage usage;
        private Apartment apartment;
        private User submittedByUser;
        private String submitterRole;
        private String feedback;
        private String note;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder usage(WaterUsage usage) { this.usage = usage; return this; }
        public Builder apartment(Apartment apartment) { this.apartment = apartment; return this; }
        public Builder submittedByUser(User submittedByUser) { this.submittedByUser = submittedByUser; return this; }
        public Builder submitterRole(String submitterRole) { this.submitterRole = submitterRole; return this; }
        public Builder feedback(String feedback) { this.feedback = feedback; return this; }
        public Builder note(String note) { this.note = note; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public LeakFeedback build() {
            return new LeakFeedback(id, usage, apartment, submittedByUser, submitterRole, feedback, note, createdAt);
        }
    }
}
