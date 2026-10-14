package com.smartwater.billing.resident.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ResidentDTOs {

    public static class InviteResidentRequest {
        @NotNull(message = "Building selection is required")
        private Long buildingId;

        @NotBlank(message = "Resident name is required")
        private String name;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Phone is required")
        private String phone;

        @NotBlank(message = "Apartment number is required")
        private String apartmentNumber;

        public InviteResidentRequest() {}
        public InviteResidentRequest(Long buildingId, String name, String email, String phone, String apartmentNumber) {
            this.buildingId = buildingId;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.apartmentNumber = apartmentNumber;
        }

        public Long getBuildingId() { return buildingId; }
        public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getApartmentNumber() { return apartmentNumber; }
        public void setApartmentNumber(String apartmentNumber) { this.apartmentNumber = apartmentNumber; }
    }

    public static class UpdateResidentRequest {
        @NotBlank(message = "Resident name is required")
        private String name;

        @NotBlank(message = "Phone is required")
        private String phone;

        @NotBlank(message = "Apartment number is required")
        private String apartmentNumber;

        public UpdateResidentRequest() {}
        public UpdateResidentRequest(String name, String phone, String apartmentNumber) {
            this.name = name;
            this.phone = phone;
            this.apartmentNumber = apartmentNumber;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getApartmentNumber() { return apartmentNumber; }
        public void setApartmentNumber(String apartmentNumber) { this.apartmentNumber = apartmentNumber; }
    }

    public static class ResidentResponse {
        private Long residentId;
        private Long buildingId;
        private String buildingName;
        private String name;
        private String email;
        private String phone;
        private String apartmentNumber;
        private String invitationToken;
        private String invitationStatus;
        private String accountStatus;
        private Long meterId;
        private String meterNumber;
        private boolean emailSent;
        private LocalDateTime createdAt;

        public ResidentResponse() {}
        public ResidentResponse(Long residentId, Long buildingId, String buildingName, String name, String email, String phone, String apartmentNumber, String invitationToken, String invitationStatus, String accountStatus, Long meterId, String meterNumber, boolean emailSent, LocalDateTime createdAt) {
            this.residentId = residentId;
            this.buildingId = buildingId;
            this.buildingName = buildingName;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.apartmentNumber = apartmentNumber;
            this.invitationToken = invitationToken;
            this.invitationStatus = invitationStatus;
            this.accountStatus = accountStatus;
            this.meterId = meterId;
            this.meterNumber = meterNumber;
            this.emailSent = emailSent;
            this.createdAt = createdAt;
        }

        public Long getResidentId() { return residentId; }
        public void setResidentId(Long residentId) { this.residentId = residentId; }

        public Long getBuildingId() { return buildingId; }
        public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }

        public String getBuildingName() { return buildingName; }
        public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

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

        public String getInvitationStatus() { return invitationStatus; }
        public void setInvitationStatus(String invitationStatus) { this.invitationStatus = invitationStatus; }

        public String getAccountStatus() { return accountStatus; }
        public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

        public Long getMeterId() { return meterId; }
        public void setMeterId(Long meterId) { this.meterId = meterId; }

        public String getMeterNumber() { return meterNumber; }
        public void setMeterNumber(String meterNumber) { this.meterNumber = meterNumber; }

        public boolean isEmailSent() { return emailSent; }
        public void setEmailSent(boolean emailSent) { this.emailSent = emailSent; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long residentId;
            private Long buildingId;
            private String buildingName;
            private String name;
            private String email;
            private String phone;
            private String apartmentNumber;
            private String invitationToken;
            private String invitationStatus;
            private String accountStatus;
            private Long meterId;
            private String meterNumber;
            private boolean emailSent;
            private LocalDateTime createdAt;

            public Builder residentId(Long residentId) { this.residentId = residentId; return this; }
            public Builder buildingId(Long buildingId) { this.buildingId = buildingId; return this; }
            public Builder buildingName(String buildingName) { this.buildingName = buildingName; return this; }
            public Builder name(String name) { this.name = name; return this; }
            public Builder email(String email) { this.email = email; return this; }
            public Builder phone(String phone) { this.phone = phone; return this; }
            public Builder apartmentNumber(String apartmentNumber) { this.apartmentNumber = apartmentNumber; return this; }
            public Builder invitationToken(String invitationToken) { this.invitationToken = invitationToken; return this; }
            public Builder invitationStatus(String invitationStatus) { this.invitationStatus = invitationStatus; return this; }
            public Builder accountStatus(String accountStatus) { this.accountStatus = accountStatus; return this; }
            public Builder meterId(Long meterId) { this.meterId = meterId; return this; }
            public Builder meterNumber(String meterNumber) { this.meterNumber = meterNumber; return this; }
            public Builder emailSent(boolean emailSent) { this.emailSent = emailSent; return this; }
            public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

            public ResidentResponse build() {
                return new ResidentResponse(residentId, buildingId, buildingName, name, email, phone, apartmentNumber, invitationToken, invitationStatus, accountStatus, meterId, meterNumber, emailSent, createdAt);
            }
        }
    }
}
