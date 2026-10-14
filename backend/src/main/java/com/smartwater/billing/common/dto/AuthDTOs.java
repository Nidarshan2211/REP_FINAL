package com.smartwater.billing.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDTOs {

    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;

        private String selectedRole;

        public LoginRequest() {}
        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
        public LoginRequest(String email, String password, String selectedRole) {
            this.email = email;
            this.password = password;
            this.selectedRole = selectedRole;
        }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getSelectedRole() { return selectedRole; }
        public void setSelectedRole(String selectedRole) { this.selectedRole = selectedRole; }
    }

    public static class LoginResponse {
        private String token;
        private String type = "Bearer";
        private Long userId;
        private String name;
        private String email;
        private String role;
        private String status;

        private String phone;

        public LoginResponse() {}
        public LoginResponse(String token, String type, Long userId, String name, String email, String phone, String role, String status) {
            this.token = token;
            this.type = type != null ? type : "Bearer";
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.role = role;
            this.status = status;
        }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

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

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String token;
            private String type = "Bearer";
            private Long userId;
            private String name;
            private String email;
            private String phone;
            private String role;
            private String status;

            public Builder token(String token) { this.token = token; return this; }
            public Builder type(String type) { this.type = type; return this; }
            public Builder userId(Long userId) { this.userId = userId; return this; }
            public Builder name(String name) { this.name = name; return this; }
            public Builder email(String email) { this.email = email; return this; }
            public Builder phone(String phone) { this.phone = phone; return this; }
            public Builder role(String role) { this.role = role; return this; }
            public Builder status(String status) { this.status = status; return this; }

            public LoginResponse build() {
                return new LoginResponse(token, type, userId, name, email, phone, role, status);
            }
        }
    }

    public static class RegisterOwnerRequest {
        @NotBlank(message = "Owner name is required")
        private String name;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        @NotBlank(message = "Phone number is required")
        private String phone;

        @NotBlank(message = "Building name is required")
        private String buildingName;

        @NotBlank(message = "Building number is required")
        private String buildingNumber;

        private Integer totalFlats;

        @NotBlank(message = "Location is required")
        private String location;

        public RegisterOwnerRequest() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getBuildingName() { return buildingName; }
        public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

        public String getBuildingNumber() { return buildingNumber; }
        public void setBuildingNumber(String buildingNumber) { this.buildingNumber = buildingNumber; }

        public Integer getTotalFlats() { return totalFlats; }
        public void setTotalFlats(Integer totalFlats) { this.totalFlats = totalFlats; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }

    public static class SetPasswordRequest {
        private String email;
        private String token;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        public SetPasswordRequest() {}
        public SetPasswordRequest(String token, String password) {
            this.token = token;
            this.password = password;
        }
        public SetPasswordRequest(String email, String token, String password) {
            this.email = email;
            this.token = token;
            this.password = password;
        }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class ValidateInvitationResponse {
        private String status; // PENDING, EXPIRED, INVALID, ALREADY_ACCEPTED
        private String residentName;
        private String email;
        private String buildingName;
        private String apartmentNumber;
        private String expiresAt;
        private String message;

        public ValidateInvitationResponse() {}
        public ValidateInvitationResponse(String status, String residentName, String email, String buildingName, String apartmentNumber, String expiresAt, String message) {
            this.status = status;
            this.residentName = residentName;
            this.email = email;
            this.buildingName = buildingName;
            this.apartmentNumber = apartmentNumber;
            this.expiresAt = expiresAt;
            this.message = message;
        }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getResidentName() { return residentName; }
        public void setResidentName(String residentName) { this.residentName = residentName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getBuildingName() { return buildingName; }
        public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

        public String getApartmentNumber() { return apartmentNumber; }
        public void setApartmentNumber(String apartmentNumber) { this.apartmentNumber = apartmentNumber; }

        public String getExpiresAt() { return expiresAt; }
        public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class MessageResponse {
        private String message;
        private boolean success = true;

        public MessageResponse() {}
        public MessageResponse(String message) {
            this.message = message;
            this.success = true;
        }
        public MessageResponse(String message, boolean success) {
            this.message = message;
            this.success = success;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
    }
}
