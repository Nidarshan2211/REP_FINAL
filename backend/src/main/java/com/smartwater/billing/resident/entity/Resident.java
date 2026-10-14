package com.smartwater.billing.resident.entity;


import com.smartwater.billing.common.entity.User;
import com.smartwater.billing.owner.entity.Building;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "residents")
public class Resident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resident_id")
    private Long residentId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "apartment_number", nullable = false, length = 20)
    private String apartmentNumber;

    @Column(name = "invitation_token")
    private String invitationToken;

    @Column(name = "token_hash")
    private String tokenHash;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "invitation_status", nullable = false, length = 50)
    private InvitationStatus invitationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 50)
    private AccountStatus accountStatus;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Resident() {}

    public Resident(Long residentId, User user, Building building, String name, String email, String phone, String apartmentNumber, String invitationToken, String tokenHash, LocalDateTime expiresAt, LocalDateTime acceptedAt, InvitationStatus invitationStatus, AccountStatus accountStatus, LocalDateTime createdAt) {
        this.residentId = residentId;
        this.user = user;
        this.building = building;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.apartmentNumber = apartmentNumber;
        this.invitationToken = invitationToken;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.acceptedAt = acceptedAt;
        this.invitationStatus = invitationStatus;
        this.accountStatus = accountStatus;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (invitationStatus == null) {
            invitationStatus = InvitationStatus.PENDING;
        }
        if (accountStatus == null) {
            accountStatus = AccountStatus.INACTIVE;
        }
    }

    public Long getResidentId() { return residentId; }
    public void setResidentId(Long residentId) { this.residentId = residentId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Building getBuilding() { return building; }
    public void setBuilding(Building building) { this.building = building; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getApartmentNumber() { return apartmentNumber; }
    public void setApartmentNumber(String apartmentNumber) { this.apartmentNumber = apartmentNumber; }

    public String getInvitationToken() { return invitationToken; }
    public void setInvitationToken(String invitationToken) { this.invitationToken = invitationToken; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }

    public InvitationStatus getInvitationStatus() { return invitationStatus; }
    public void setInvitationStatus(InvitationStatus invitationStatus) { this.invitationStatus = invitationStatus; }

    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long residentId;
        private User user;
        private Building building;
        private String name;
        private String email;
        private String phone;
        private String apartmentNumber;
        private String invitationToken;
        private String tokenHash;
        private LocalDateTime expiresAt;
        private LocalDateTime acceptedAt;
        private InvitationStatus invitationStatus;
        private AccountStatus accountStatus;
        private LocalDateTime createdAt;

        public Builder residentId(Long residentId) { this.residentId = residentId; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder building(Building building) { this.building = building; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder apartmentNumber(String apartmentNumber) { this.apartmentNumber = apartmentNumber; return this; }
        public Builder invitationToken(String invitationToken) { this.invitationToken = invitationToken; return this; }
        public Builder tokenHash(String tokenHash) { this.tokenHash = tokenHash; return this; }
        public Builder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder acceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; return this; }
        public Builder invitationStatus(InvitationStatus invitationStatus) { this.invitationStatus = invitationStatus; return this; }
        public Builder accountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Resident build() {
            return new Resident(residentId, user, building, name, email, phone, apartmentNumber, invitationToken, tokenHash, expiresAt, acceptedAt, invitationStatus, accountStatus, createdAt);
        }
    }

    public enum InvitationStatus { PENDING, ACCEPTED, EXPIRED, CANCELLED }
    public enum AccountStatus { ACTIVE, INACTIVE }
}
