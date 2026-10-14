package com.smartwater.billing.owner.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "apartments", uniqueConstraints = {
    @UniqueConstraint(name = "uk_building_room", columnNames = {"building_id", "room_number"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Apartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "apartment_id")
    private Long apartmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "owner"})
    private Building building;

    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;

    @Column(name = "apartment_name", nullable = false, length = 100)
    private String apartmentName;

    @Column(name = "room_number", nullable = false, length = 50)
    private String roomNumber;

    @Column(name = "unit_type", length = 50)
    private String unitType;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Apartment() {}

    public Apartment(Long apartmentId, Building building, Integer floorNumber, String apartmentName, String roomNumber, String unitType, String description, LocalDateTime createdAt) {
        this.apartmentId = apartmentId;
        this.building = building;
        this.floorNumber = floorNumber;
        this.apartmentName = apartmentName;
        this.roomNumber = roomNumber;
        this.unitType = unitType;
        this.description = description;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (floorNumber == null) {
            floorNumber = 1;
        }
    }

    public Long getApartmentId() { return apartmentId; }
    public void setApartmentId(Long apartmentId) { this.apartmentId = apartmentId; }

    public Building getBuilding() { return building; }
    public void setBuilding(Building building) { this.building = building; }

    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }

    public String getApartmentName() { return apartmentName; }
    public void setApartmentName(String apartmentName) { this.apartmentName = apartmentName; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getUnitType() { return unitType; }
    public void setUnitType(String unitType) { this.unitType = unitType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long apartmentId;
        private Building building;
        private Integer floorNumber = 1;
        private String apartmentName;
        private String roomNumber;
        private String unitType;
        private String description;
        private LocalDateTime createdAt;

        public Builder apartmentId(Long apartmentId) { this.apartmentId = apartmentId; return this; }
        public Builder building(Building building) { this.building = building; return this; }
        public Builder floorNumber(Integer floorNumber) { this.floorNumber = floorNumber; return this; }
        public Builder apartmentName(String apartmentName) { this.apartmentName = apartmentName; return this; }
        public Builder roomNumber(String roomNumber) { this.roomNumber = roomNumber; return this; }
        public Builder unitType(String unitType) { this.unitType = unitType; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Apartment build() {
            return new Apartment(apartmentId, building, floorNumber, apartmentName, roomNumber, unitType, description, createdAt);
        }
    }
}
