package com.smartwater.billing.owner.entity;


import com.smartwater.billing.common.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "building_owners")
public class BuildingOwner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "owner_id")
    private Long ownerId;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 50)
    private ApprovalStatus approvalStatus;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public BuildingOwner() {}

    public BuildingOwner(Long ownerId, User user, String name, String phone, ApprovalStatus approvalStatus, LocalDateTime createdAt) {
        this.ownerId = ownerId;
        this.user = user;
        this.name = name;
        this.phone = phone;
        this.approvalStatus = approvalStatus;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (approvalStatus == null) {
            approvalStatus = ApprovalStatus.PENDING;
        }
        if (name == null && user != null) {
            name = user.getName();
        }
    }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public ApprovalStatus getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(ApprovalStatus approvalStatus) { this.approvalStatus = approvalStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long ownerId;
        private User user;
        private String name;
        private String phone;
        private ApprovalStatus approvalStatus;
        private LocalDateTime createdAt;

        public Builder ownerId(Long ownerId) { this.ownerId = ownerId; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder approvalStatus(ApprovalStatus approvalStatus) { this.approvalStatus = approvalStatus; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public BuildingOwner build() {
            return new BuildingOwner(ownerId, user, name, phone, approvalStatus, createdAt);
        }
    }

    public enum ApprovalStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}
