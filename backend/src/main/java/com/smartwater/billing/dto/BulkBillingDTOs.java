package com.smartwater.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class BulkBillingDTOs {

    public enum RowStatus {
        VALID,
        INVALID,
        ALREADY_BILLED,
        DUPLICATE_ROW,
        MISSING_IN_FILE
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkReadingRowDTO {
        private Integer rowNumber;
        private Long residentId;
        private String residentName;
        private String apartmentNumber;
        private Long meterId;
        private String meterNumber;
        private Double previousReading;
        private Double currentReading;
        private Double unitsConsumed;
        private Double estimatedCharge;
        private RowStatus status;
        private String errorMessage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkValidationResponse {
        private Long buildingId;
        private String buildingName;
        private Long billingCycleId;
        private String cyclePeriod;
        private String cycleStatus;
        
        private int totalResidentsCount;
        private int totalRowsInCsv;
        private int validCount;
        private int errorCount;
        private int alreadyBilledCount;
        private int missingResidentsCount;
        
        private Double totalEstimatedUnits;
        private Double totalEstimatedAmount;
        
        private boolean canFinalize;
        private String statusMessage;
        
        @Builder.Default
        private List<String> missingResidents = new ArrayList<>();
        
        @Builder.Default
        private List<BulkReadingRowDTO> rows = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CycleReadinessDTO {
        private Long buildingId;
        private String buildingName;
        private Long billingCycleId;
        private String cycleStatus;
        private String startDate;
        private String endDate;
        private int eligibleResidentsCount;
        private int recordedReadingsCount;
        private int missingReadingsCount;
        private int existingBillsCount;
        private boolean canImport;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkImportResponse {
        private Long buildingId;
        private String buildingName;
        private Long billingCycleId;
        private String cyclePeriod;
        private int billsGeneratedCount;
        private Double totalUnitsBilled;
        private Double totalAmountBilled;
        private int anomaliesDetected;
        private boolean cycleFinalized;
        private String cycleStatus;
        private String message;
    }
}
