package com.smartwater.billing.owner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class ApartmentDTOs {

    public static class CreateApartmentRequest {
        private Long buildingId;

        @NotNull(message = "Floor number is required")
        private Integer floorNumber;

        @NotBlank(message = "Apartment name is required")
        private String apartmentName;

        @NotBlank(message = "Room/flat number is required")
        private String roomNumber;

        private String unitType;
        private String description;

        public CreateApartmentRequest() {}

        public Long getBuildingId() { return buildingId; }
        public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }

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
    }

    public static class BatchCreateApartmentsRequest {
        @NotNull(message = "Building ID is required")
        private Long buildingId;

        @NotNull(message = "Floor number is required")
        private Integer floorNumber;

        @NotNull(message = "Room numbers list is required")
        private List<String> roomNumbers;

        private String unitType;

        public BatchCreateApartmentsRequest() {}

        public Long getBuildingId() { return buildingId; }
        public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }

        public Integer getFloorNumber() { return floorNumber; }
        public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }

        public List<String> getRoomNumbers() { return roomNumbers; }
        public void setRoomNumbers(List<String> roomNumbers) { this.roomNumbers = roomNumbers; }

        public String getUnitType() { return unitType; }
        public void setUnitType(String unitType) { this.unitType = unitType; }
    }

    public static class ApartmentResponse {
        private Long apartmentId;
        private Long buildingId;
        private String buildingName;
        private String buildingNumber;
        private String location;
        private Integer floorNumber;
        private String apartmentName;
        private String roomNumber;
        private String unitType;
        private String description;
        private String occupancyStatus; // VACANT, OCCUPIED, PENDING_INVITATION, PENDING_APPROVAL
        private Long currentResidentId;
        private String currentResidentName;
        private String currentResidentEmail;
        private String currentResidentPhone;
        private String meterNumber;
        private LocalDateTime occupiedSince;
        private LocalDateTime createdAt;
        private Long pendingRequestId;
        private Long pendingInvitationId;
        private Long activeOccupancyId;

        public ApartmentResponse() {}

        public Long getApartmentId() { return apartmentId; }
        public void setApartmentId(Long apartmentId) { this.apartmentId = apartmentId; }

        public Long getBuildingId() { return buildingId; }
        public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }

        public String getBuildingName() { return buildingName; }
        public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

        public String getBuildingNumber() { return buildingNumber; }
        public void setBuildingNumber(String buildingNumber) { this.buildingNumber = buildingNumber; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

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

        public String getOccupancyStatus() { return occupancyStatus; }
        public void setOccupancyStatus(String occupancyStatus) { this.occupancyStatus = occupancyStatus; }

        public Long getCurrentResidentId() { return currentResidentId; }
        public void setCurrentResidentId(Long currentResidentId) { this.currentResidentId = currentResidentId; }

        public String getCurrentResidentName() { return currentResidentName; }
        public void setCurrentResidentName(String currentResidentName) { this.currentResidentName = currentResidentName; }

        public String getCurrentResidentEmail() { return currentResidentEmail; }
        public void setCurrentResidentEmail(String currentResidentEmail) { this.currentResidentEmail = currentResidentEmail; }

        public String getCurrentResidentPhone() { return currentResidentPhone; }
        public void setCurrentResidentPhone(String currentResidentPhone) { this.currentResidentPhone = currentResidentPhone; }

        public String getMeterNumber() { return meterNumber; }
        public void setMeterNumber(String meterNumber) { this.meterNumber = meterNumber; }

        public LocalDateTime getOccupiedSince() { return occupiedSince; }
        public void setOccupiedSince(LocalDateTime occupiedSince) { this.occupiedSince = occupiedSince; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public Long getPendingRequestId() { return pendingRequestId; }
        public void setPendingRequestId(Long pendingRequestId) { this.pendingRequestId = pendingRequestId; }

        public Long getPendingInvitationId() { return pendingInvitationId; }
        public void setPendingInvitationId(Long pendingInvitationId) { this.pendingInvitationId = pendingInvitationId; }

        public Long getActiveOccupancyId() { return activeOccupancyId; }
        public void setActiveOccupancyId(Long activeOccupancyId) { this.activeOccupancyId = activeOccupancyId; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long apartmentId;
            private Long buildingId;
            private String buildingName;
            private String buildingNumber;
            private String location;
            private Integer floorNumber;
            private String apartmentName;
            private String roomNumber;
            private String unitType;
            private String description;
            private String occupancyStatus;
            private Long currentResidentId;
            private String currentResidentName;
            private String currentResidentEmail;
            private String currentResidentPhone;
            private String meterNumber;
            private LocalDateTime occupiedSince;
            private LocalDateTime createdAt;
            private Long pendingRequestId;
            private Long pendingInvitationId;
            private Long activeOccupancyId;

            public Builder apartmentId(Long id) { this.apartmentId = id; return this; }
            public Builder buildingId(Long id) { this.buildingId = id; return this; }
            public Builder buildingName(String n) { this.buildingName = n; return this; }
            public Builder buildingNumber(String n) { this.buildingNumber = n; return this; }
            public Builder location(String l) { this.location = l; return this; }
            public Builder floorNumber(Integer f) { this.floorNumber = f; return this; }
            public Builder apartmentName(String n) { this.apartmentName = n; return this; }
            public Builder roomNumber(String r) { this.roomNumber = r; return this; }
            public Builder unitType(String t) { this.unitType = t; return this; }
            public Builder description(String d) { this.description = d; return this; }
            public Builder occupancyStatus(String s) { this.occupancyStatus = s; return this; }
            public Builder currentResidentId(Long id) { this.currentResidentId = id; return this; }
            public Builder currentResidentName(String n) { this.currentResidentName = n; return this; }
            public Builder currentResidentEmail(String e) { this.currentResidentEmail = e; return this; }
            public Builder currentResidentPhone(String p) { this.currentResidentPhone = p; return this; }
            public Builder meterNumber(String m) { this.meterNumber = m; return this; }
            public Builder occupiedSince(LocalDateTime d) { this.occupiedSince = d; return this; }
            public Builder createdAt(LocalDateTime d) { this.createdAt = d; return this; }
            public Builder pendingRequestId(Long id) { this.pendingRequestId = id; return this; }
            public Builder pendingInvitationId(Long id) { this.pendingInvitationId = id; return this; }
            public Builder activeOccupancyId(Long id) { this.activeOccupancyId = id; return this; }

            public ApartmentResponse build() {
                ApartmentResponse r = new ApartmentResponse();
                r.setApartmentId(apartmentId);
                r.setBuildingId(buildingId);
                r.setBuildingName(buildingName);
                r.setBuildingNumber(buildingNumber);
                r.setLocation(location);
                r.setFloorNumber(floorNumber);
                r.setApartmentName(apartmentName);
                r.setRoomNumber(roomNumber);
                r.setUnitType(unitType);
                r.setDescription(description);
                r.setOccupancyStatus(occupancyStatus);
                r.setCurrentResidentId(currentResidentId);
                r.setCurrentResidentName(currentResidentName);
                r.setCurrentResidentEmail(currentResidentEmail);
                r.setCurrentResidentPhone(currentResidentPhone);
                r.setMeterNumber(meterNumber);
                r.setOccupiedSince(occupiedSince);
                r.setCreatedAt(createdAt);
                r.setPendingRequestId(pendingRequestId);
                r.setPendingInvitationId(pendingInvitationId);
                r.setActiveOccupancyId(activeOccupancyId);
                return r;
            }
        }
    }

    public static class FloorGroupResponse {
        private Integer floorNumber;
        private List<ApartmentResponse> apartments;

        public FloorGroupResponse() {}

        public FloorGroupResponse(Integer floorNumber, List<ApartmentResponse> apartments) {
            this.floorNumber = floorNumber;
            this.apartments = apartments;
        }

        public Integer getFloorNumber() { return floorNumber; }
        public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }

        public List<ApartmentResponse> getApartments() { return apartments; }
        public void setApartments(List<ApartmentResponse> apartments) { this.apartments = apartments; }
    }

    public static class BuildingInventoryResponse {
        private Long buildingId;
        private String buildingName;
        private String buildingNumber;
        private String location;
        private Integer totalFlats;
        private int configuredUnitsCount;
        private int occupiedCount;
        private int vacantCount;
        private int pendingCount;
        private List<FloorGroupResponse> floors;

        public BuildingInventoryResponse() {}

        public Long getBuildingId() { return buildingId; }
        public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }

        public String getBuildingName() { return buildingName; }
        public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

        public String getBuildingNumber() { return buildingNumber; }
        public void setBuildingNumber(String buildingNumber) { this.buildingNumber = buildingNumber; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public Integer getTotalFlats() { return totalFlats; }
        public void setTotalFlats(Integer totalFlats) { this.totalFlats = totalFlats; }

        public int getConfiguredUnitsCount() { return configuredUnitsCount; }
        public void setConfiguredUnitsCount(int configuredUnitsCount) { this.configuredUnitsCount = configuredUnitsCount; }

        public int getOccupiedCount() { return occupiedCount; }
        public void setOccupiedCount(int occupiedCount) { this.occupiedCount = occupiedCount; }

        public int getVacantCount() { return vacantCount; }
        public void setVacantCount(int vacantCount) { this.vacantCount = vacantCount; }

        public int getPendingCount() { return pendingCount; }
        public void setPendingCount(int pendingCount) { this.pendingCount = pendingCount; }

        public List<FloorGroupResponse> getFloors() { return floors; }
        public void setFloors(List<FloorGroupResponse> floors) { this.floors = floors; }
    }
}
