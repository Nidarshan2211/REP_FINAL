package com.smartwater.billing.owner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class BuildingDTOs {

    public static class CreateBuildingRequest {
        @NotBlank(message = "Building name is required")
        private String buildingName;

        @NotBlank(message = "Building number is required")
        private String buildingNumber;

        @NotNull(message = "Total flats is required")
        private Integer totalFlats;

        @NotBlank(message = "Location is required")
        private String location;

        public CreateBuildingRequest() {}
        public CreateBuildingRequest(String buildingName, String buildingNumber, Integer totalFlats, String location) {
            this.buildingName = buildingName;
            this.buildingNumber = buildingNumber;
            this.totalFlats = totalFlats;
            this.location = location;
        }

        public String getBuildingName() { return buildingName; }
        public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

        public String getBuildingNumber() { return buildingNumber; }
        public void setBuildingNumber(String buildingNumber) { this.buildingNumber = buildingNumber; }

        public Integer getTotalFlats() { return totalFlats; }
        public void setTotalFlats(Integer totalFlats) { this.totalFlats = totalFlats; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }

    public static class BuildingResponse {
        private Long buildingId;
        private Long ownerId;
        private String ownerName;
        private String buildingName;
        private String buildingNumber;
        private Integer totalFlats;
        private String location;
        private LocalDateTime createdAt;

        public BuildingResponse() {}
        public BuildingResponse(Long buildingId, Long ownerId, String ownerName, String buildingName, String buildingNumber, Integer totalFlats, String location, LocalDateTime createdAt) {
            this.buildingId = buildingId;
            this.ownerId = ownerId;
            this.ownerName = ownerName;
            this.buildingName = buildingName;
            this.buildingNumber = buildingNumber;
            this.totalFlats = totalFlats;
            this.location = location;
            this.createdAt = createdAt;
        }

        public Long getBuildingId() { return buildingId; }
        public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }

        public Long getOwnerId() { return ownerId; }
        public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

        public String getOwnerName() { return ownerName; }
        public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

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
            private Long ownerId;
            private String ownerName;
            private String buildingName;
            private String buildingNumber;
            private Integer totalFlats;
            private String location;
            private LocalDateTime createdAt;

            public Builder buildingId(Long buildingId) { this.buildingId = buildingId; return this; }
            public Builder ownerId(Long ownerId) { this.ownerId = ownerId; return this; }
            public Builder ownerName(String ownerName) { this.ownerName = ownerName; return this; }
            public Builder buildingName(String buildingName) { this.buildingName = buildingName; return this; }
            public Builder buildingNumber(String buildingNumber) { this.buildingNumber = buildingNumber; return this; }
            public Builder totalFlats(Integer totalFlats) { this.totalFlats = totalFlats; return this; }
            public Builder location(String location) { this.location = location; return this; }
            public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

            public BuildingResponse build() {
                return new BuildingResponse(buildingId, ownerId, ownerName, buildingName, buildingNumber, totalFlats, location, createdAt);
            }
        }
    }
}
