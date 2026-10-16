package com.smartwater.billing.resident.entity;

import com.smartwater.billing.owner.entity.Apartment;
import com.smartwater.billing.owner.entity.Building;
import com.smartwater.billing.owner.entity.BuildingOwner;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resident_invitations")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ResidentInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user"})
    private Resident resident;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "building_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "owner"})
    private Building building;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Apartment apartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user"})
    private BuildingOwner invitedBy;

    @Column(name = "resident_name", nullable = false, length = 100)
    private String residentName;

    @Column(name = "resident_email", nullable = false, length = 150)
    private String residentEmail;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "apartment_number", nullable = false, length = 20)
    private String apartmentNumber;

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private InvitationStatus status;

    @Column(name = "household_size")
    private Integer householdSize;

    @Column(name = "children_count")
    private Integer childrenCount;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    public ResidentInvitation() {}

    public ResidentInvitation(Long id, Resident resident, Building building, Apartment apartment, BuildingOwner invitedBy, String residentName, String residentEmail, String phoneNumber, String apartmentNumber, String tokenHash, LocalDateTime expiresAt, InvitationStatus status, LocalDateTime createdAt, LocalDateTime acceptedAt) {
        this.id = id;
        this.resident = resident;
        this.building = building;
        this.apartment = apartment;
        this.invitedBy = invitedBy;
        this.residentName = residentName;
        this.residentEmail = residentEmail;
        this.phoneNumber = phoneNumber;
        this.apartmentNumber = apartmentNumber;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.status = status;
        this.createdAt = createdAt;
        this.acceptedAt = acceptedAt;
    }

    public ResidentInvitation(Long id, Resident resident, Building building, BuildingOwner invitedBy, String residentName, String residentEmail, String phoneNumber, String apartmentNumber, String tokenHash, LocalDateTime expiresAt, InvitationStatus status, LocalDateTime createdAt, LocalDateTime acceptedAt) {
        this(id, resident, building, null, invitedBy, residentName, residentEmail, phoneNumber, apartmentNumber, tokenHash, expiresAt, status, createdAt, acceptedAt);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = InvitationStatus.PENDING;
        if (expiresAt == null) expiresAt = LocalDateTime.now().plusHours(24);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Resident getResident() { return resident; }
    public void setResident(Resident resident) { this.resident = resident; }

    public Building getBuilding() { return building; }
    public void setBuilding(Building building) { this.building = building; }

    public Apartment getApartment() { return apartment; }
    public void setApartment(Apartment apartment) { this.apartment = apartment; }

    public BuildingOwner getInvitedBy() { return invitedBy; }
    public void setInvitedBy(BuildingOwner invitedBy) { this.invitedBy = invitedBy; }

    public String getResidentName() { return residentName; }
    public void setResidentName(String residentName) { this.residentName = residentName; }

    public String getResidentEmail() { return residentEmail; }
    public void setResidentEmail(String residentEmail) { this.residentEmail = residentEmail; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getApartmentNumber() { return apartmentNumber; }
    public void setApartmentNumber(String apartmentNumber) { this.apartmentNumber = apartmentNumber; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public InvitationStatus getStatus() { return status; }
    public void setStatus(InvitationStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }

    public Integer getHouseholdSize() { return householdSize; }
    public void setHouseholdSize(Integer householdSize) { this.householdSize = householdSize; }

    public Integer getChildrenCount() { return childrenCount; }
    public void setChildrenCount(Integer childrenCount) { this.childrenCount = childrenCount; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Resident resident;
        private Building building;
        private Apartment apartment;
        private BuildingOwner invitedBy;
        private String residentName;
        private String residentEmail;
        private String phoneNumber;
        private String apartmentNumber;
        private String tokenHash;
        private LocalDateTime expiresAt;
        private InvitationStatus status;
        private Integer householdSize;
        private Integer childrenCount;
        private LocalDateTime createdAt;
        private LocalDateTime acceptedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder resident(Resident resident) { this.resident = resident; return this; }
        public Builder building(Building building) { this.building = building; return this; }
        public Builder apartment(Apartment apartment) { this.apartment = apartment; return this; }
        public Builder invitedBy(BuildingOwner invitedBy) { this.invitedBy = invitedBy; return this; }
        public Builder residentName(String residentName) { this.residentName = residentName; return this; }
        public Builder residentEmail(String residentEmail) { this.residentEmail = residentEmail; return this; }
        public Builder phoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; return this; }
        public Builder apartmentNumber(String apartmentNumber) { this.apartmentNumber = apartmentNumber; return this; }
        public Builder tokenHash(String tokenHash) { this.tokenHash = tokenHash; return this; }
        public Builder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder status(InvitationStatus status) { this.status = status; return this; }
        public Builder householdSize(Integer householdSize) { this.householdSize = householdSize; return this; }
        public Builder childrenCount(Integer childrenCount) { this.childrenCount = childrenCount; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder acceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; return this; }

        public ResidentInvitation build() {
            ResidentInvitation inv = new ResidentInvitation(id, resident, building, apartment, invitedBy, residentName, residentEmail, phoneNumber, apartmentNumber, tokenHash, expiresAt, status, createdAt, acceptedAt);
            inv.setHouseholdSize(householdSize);
            inv.setChildrenCount(childrenCount);
            return inv;
        }
    }

    public enum InvitationStatus {
        PENDING,
        ACCEPTED,
        EXPIRED,
        CANCELLED
    }
}
