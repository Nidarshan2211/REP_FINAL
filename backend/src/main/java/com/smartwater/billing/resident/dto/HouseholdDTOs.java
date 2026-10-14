package com.smartwater.billing.resident.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

public class HouseholdDTOs {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HouseholdRequest {
        @NotNull(message = "Apartment ID is required")
        private Long apartmentId;

        @NotBlank(message = "Unit number is required")
        private String unitNumber;

        private Double areaSqft = 1000.0;
        private Integer occupancyCount = 1;
        private Boolean isMetered = true;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InviteResidentRequest {
        @NotNull(message = "Household ID is required")
        private Long householdId;

        @NotBlank(message = "Resident name is required")
        private String residentName;

        @NotBlank(message = "Resident email is required")
        private String residentEmail;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeterRequest {
        @NotNull(message = "Household ID is required")
        private Long householdId;

        @NotBlank(message = "Meter serial is required")
        private String meterSerial;

        private Double initialReading = 0.0;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class HouseholdResponse {
        private Long id;
        private Long apartmentId;
        private String unitNumber;
        private Long residentId;
        private String residentName;
        private String residentEmail;
        private String residentStatus;
        private Double areaSqft;
        private Integer occupancyCount;
        private Boolean isMetered;
        private String meterSerial;
        private Double latestMeterReading;
    }
}
