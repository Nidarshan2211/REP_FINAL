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

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HouseholdUpdateRequest {
        @NotNull(message = "Household size is required")
        private Integer householdSize;

        private Integer childrenCount;
        private String reason;
        private String note;
        private Boolean confirmSanityWarning;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HouseholdConfirmRequest {
        private Boolean confirmed;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HouseholdDetailResponse {
        private Long occupancyId;
        private Long apartmentId;
        private String apartmentNumber;
        private Integer householdSize;
        private Integer childrenCount;
        private String householdSizeSource;
        private Boolean householdSizeConfirmed;
        private Boolean sanityWarning;
        private java.util.List<HouseholdHistoryItemResponse> history;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HouseholdHistoryItemResponse {
        private Long id;
        private Integer householdSize;
        private Integer childrenCount;
        private java.time.LocalDate effectiveFrom;
        private java.time.LocalDate effectiveTo;
        private String changedByRole;
        private String reason;
        private String note;
        private Boolean sanityWarning;
        private String status;
        private java.time.LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AwayPeriodRequest {
        @NotNull(message = "Start date is required")
        private java.time.LocalDate startDate;

        @NotNull(message = "End date is required")
        private java.time.LocalDate endDate;

        private String note;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AwayPeriodResponse {
        private Long id;
        private Long occupancyId;
        private Long apartmentId;
        private java.time.LocalDate startDate;
        private java.time.LocalDate endDate;
        private Integer durationDays;
        private String note;
        private java.time.LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BuildingHouseholdStatusResponse {
        private Long apartmentId;
        private String apartmentNumber;
        private Integer floorNumber;
        private String unitType;
        private String residentName;
        private String residentEmail;
        private Integer householdSize;
        private Integer childrenCount;
        private String householdSizeSource;
        private Boolean householdSizeConfirmed;
        private java.time.LocalDateTime lastUpdated;
    }
}
