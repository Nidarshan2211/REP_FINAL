package com.smartwater.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class UserDTOs {

    public static class UserProfileResponse {
        private Long userId;
        private String name;
        private String email;
        private String phone;
        private String role;
        private String status;

        // Resident fields
        private Long residentId;
        private String buildingName;
        private String apartmentNumber;
        private String meterNumber;

        // Owner fields
        private Long ownerId;
        private Integer totalBuildings;
        private Integer totalResidents;

        // Admin fields
        private String systemPrivilege;

        private LocalDateTime createdAt;

        public UserProfileResponse() {}
        public UserProfileResponse(Long userId, String name, String email, String phone, String role, String status, Long residentId, String buildingName, String apartmentNumber, String meterNumber, Long ownerId, Integer totalBuildings, Integer totalResidents, String systemPrivilege, LocalDateTime createdAt) {
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.role = role;
            this.status = status;
            this.residentId = residentId;
            this.buildingName = buildingName;
            this.apartmentNumber = apartmentNumber;
            this.meterNumber = meterNumber;
            this.ownerId = ownerId;
            this.totalBuildings = totalBuildings;
            this.totalResidents = totalResidents;
            this.systemPrivilege = systemPrivilege;
            this.createdAt = createdAt;
        }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Long getResidentId() { return residentId; }
        public void setResidentId(Long residentId) { this.residentId = residentId; }

        public String getBuildingName() { return buildingName; }
        public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

        public String getApartmentNumber() { return apartmentNumber; }
        public void setApartmentNumber(String apartmentNumber) { this.apartmentNumber = apartmentNumber; }

        public String getMeterNumber() { return meterNumber; }
        public void setMeterNumber(String meterNumber) { this.meterNumber = meterNumber; }

        public Long getOwnerId() { return ownerId; }
        public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

        public Integer getTotalBuildings() { return totalBuildings; }
        public void setTotalBuildings(Integer totalBuildings) { this.totalBuildings = totalBuildings; }

        public Integer getTotalResidents() { return totalResidents; }
        public void setTotalResidents(Integer totalResidents) { this.totalResidents = totalResidents; }

        public String getSystemPrivilege() { return systemPrivilege; }
        public void setSystemPrivilege(String systemPrivilege) { this.systemPrivilege = systemPrivilege; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long userId;
            private String name;
            private String email;
            private String phone;
            private String role;
            private String status;
            private Long residentId;
            private String buildingName;
            private String apartmentNumber;
            private String meterNumber;
            private Long ownerId;
            private Integer totalBuildings;
            private Integer totalResidents;
            private String systemPrivilege;
            private LocalDateTime createdAt;

            public Builder userId(Long userId) { this.userId = userId; return this; }
            public Builder name(String name) { this.name = name; return this; }
            public Builder email(String email) { this.email = email; return this; }
            public Builder phone(String phone) { this.phone = phone; return this; }
            public Builder role(String role) { this.role = role; return this; }
            public Builder status(String status) { this.status = status; return this; }
            public Builder residentId(Long residentId) { this.residentId = residentId; return this; }
            public Builder buildingName(String buildingName) { this.buildingName = buildingName; return this; }
            public Builder apartmentNumber(String apartmentNumber) { this.apartmentNumber = apartmentNumber; return this; }
            public Builder meterNumber(String meterNumber) { this.meterNumber = meterNumber; return this; }
            public Builder ownerId(Long ownerId) { this.ownerId = ownerId; return this; }
            public Builder totalBuildings(Integer totalBuildings) { this.totalBuildings = totalBuildings; return this; }
            public Builder totalResidents(Integer totalResidents) { this.totalResidents = totalResidents; return this; }
            public Builder systemPrivilege(String systemPrivilege) { this.systemPrivilege = systemPrivilege; return this; }
            public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

            public UserProfileResponse build() {
                return new UserProfileResponse(userId, name, email, phone, role, status, residentId, buildingName, apartmentNumber, meterNumber, ownerId, totalBuildings, totalResidents, systemPrivilege, createdAt);
            }
        }
    }

    public static class UpdateProfileRequest {
        @NotBlank(message = "Name cannot be empty")
        private String name;
        private String phone;

        public UpdateProfileRequest() {}
        public UpdateProfileRequest(String name, String phone) {
            this.name = name;
            this.phone = phone;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    public static class ChangePasswordRequest {
        @NotBlank(message = "Current password is required")
        private String currentPassword;

        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String newPassword;

        public ChangePasswordRequest() {}
        public ChangePasswordRequest(String currentPassword, String newPassword) {
            this.currentPassword = currentPassword;
            this.newPassword = newPassword;
        }

        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}
