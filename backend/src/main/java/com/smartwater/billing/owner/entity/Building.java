package com.smartwater.billing.owner.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "buildings")
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "building_id")
    private Long buildingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private BuildingOwner owner;

    @Column(name = "building_name", nullable = false, length = 150)
    private String buildingName;

    @Column(name = "building_number", nullable = false, length = 50)
    private String buildingNumber;

    @Column(name = "total_flats", nullable = false)
    private Integer totalFlats;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Building() {}

    public Building(Long buildingId, BuildingOwner owner, String buildingName, String buildingNumber, Integer totalFlats, String location, LocalDateTime createdAt) {
        this.buildingId = buildingId;
        this.owner = owner;
        this.buildingName = buildingName;
        this.buildingNumber = buildingNumber;
        this.totalFlats = totalFlats;
        this.location = location;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getBuildingId() { return buildingId; }
    public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }

    public BuildingOwner getOwner() { return owner; }
    public void setOwner(BuildingOwner owner) { this.owner = owner; }

    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    public String getBuildingNumber() { return buildingNumber; }
    public void setBuildingNumber(String buildingNumber) { this.buildingNumber = buildingNumber; }

    public Integer getTotalFlats() { return totalFlats; }
    public void setTotalFlats(Integer totalFlats) { this.totalFlats = totalFlats; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long buildingId;
        private BuildingOwner owner;
        private String buildingName;
        private String buildingNumber;
        private Integer totalFlats;
        private String location;
        private LocalDateTime createdAt;

        public Builder buildingId(Long buildingId) { this.buildingId = buildingId; return this; }
        public Builder owner(BuildingOwner owner) { this.owner = owner; return this; }
        public Builder buildingName(String buildingName) { this.buildingName = buildingName; return this; }
        public Builder buildingNumber(String buildingNumber) { this.buildingNumber = buildingNumber; return this; }
        public Builder totalFlats(Integer totalFlats) { this.totalFlats = totalFlats; return this; }
        public Builder location(String location) { this.location = location; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Building build() {
            return new Building(buildingId, owner, buildingName, buildingNumber, totalFlats, location, createdAt);
        }
    }
}
