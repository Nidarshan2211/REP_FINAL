package com.smartwater.billing.common.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class UsageDTOs {

    public static class RecordUsageRequest {
        @NotNull(message = "Meter ID is required")
        private Long meterId;

        private Long billingCycleId;
        private Integer month;
        private Integer year;

        @NotNull(message = "Previous reading is required")
        private Double previousReading;

        @NotNull(message = "Current reading is required")
        private Double currentReading;

        public RecordUsageRequest() {}
        public RecordUsageRequest(Long meterId, Long billingCycleId, Integer month, Integer year, Double previousReading, Double currentReading) {
            this.meterId = meterId;
            this.billingCycleId = billingCycleId;
            this.month = month;
            this.year = year;
            this.previousReading = previousReading;
            this.currentReading = currentReading;
        }

        public Long getMeterId() { return meterId; }
        public void setMeterId(Long meterId) { this.meterId = meterId; }

        public Long getBillingCycleId() { return billingCycleId; }
        public void setBillingCycleId(Long billingCycleId) { this.billingCycleId = billingCycleId; }

        public Integer getMonth() { return month; }
        public void setMonth(Integer month) { this.month = month; }

        public Integer getYear() { return year; }
        public void setYear(Integer year) { this.year = year; }

        public Double getPreviousReading() { return previousReading; }
        public void setPreviousReading(Double previousReading) { this.previousReading = previousReading; }

        public Double getCurrentReading() { return currentReading; }
        public void setCurrentReading(Double currentReading) { this.currentReading = currentReading; }
    }

    public static class UsageResponse {
        private Long usageId;
        private Long meterId;
        private String meterNumber;
        private Long residentId;
        private String residentName;
        private Long billingCycleId;
        private Integer month;
        private Integer year;
        private Double previousReading;
        private Double currentReading;
        private Double unitsConsumed;
        private String anomalyStatus;
        private LocalDateTime recordedAt;

        public UsageResponse() {}
        public UsageResponse(Long usageId, Long meterId, String meterNumber, Long residentId, String residentName, Long billingCycleId, Integer month, Integer year, Double previousReading, Double currentReading, Double unitsConsumed, String anomalyStatus, LocalDateTime recordedAt) {
            this.usageId = usageId;
            this.meterId = meterId;
            this.meterNumber = meterNumber;
            this.residentId = residentId;
            this.residentName = residentName;
            this.billingCycleId = billingCycleId;
            this.month = month;
            this.year = year;
            this.previousReading = previousReading;
            this.currentReading = currentReading;
            this.unitsConsumed = unitsConsumed;
            this.anomalyStatus = anomalyStatus;
            this.recordedAt = recordedAt;
        }

        public Long getUsageId() { return usageId; }
        public void setUsageId(Long usageId) { this.usageId = usageId; }

        public Long getMeterId() { return meterId; }
        public void setMeterId(Long meterId) { this.meterId = meterId; }

        public String getMeterNumber() { return meterNumber; }
        public void setMeterNumber(String meterNumber) { this.meterNumber = meterNumber; }

        public Long getResidentId() { return residentId; }
        public void setResidentId(Long residentId) { this.residentId = residentId; }

        public String getResidentName() { return residentName; }
        public void setResidentName(String residentName) { this.residentName = residentName; }

        public Long getBillingCycleId() { return billingCycleId; }
        public void setBillingCycleId(Long billingCycleId) { this.billingCycleId = billingCycleId; }

        public Integer getMonth() { return month; }
        public void setMonth(Integer month) { this.month = month; }

        public Integer getYear() { return year; }
        public void setYear(Integer year) { this.year = year; }

        public Double getPreviousReading() { return previousReading; }
        public void setPreviousReading(Double previousReading) { this.previousReading = previousReading; }

        public Double getCurrentReading() { return currentReading; }
        public void setCurrentReading(Double currentReading) { this.currentReading = currentReading; }

        public Double getUnitsConsumed() { return unitsConsumed; }
        public void setUnitsConsumed(Double unitsConsumed) { this.unitsConsumed = unitsConsumed; }

        public String getAnomalyStatus() { return anomalyStatus; }
        public void setAnomalyStatus(String anomalyStatus) { this.anomalyStatus = anomalyStatus; }

        public LocalDateTime getRecordedAt() { return recordedAt; }
        public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long usageId;
            private Long meterId;
            private String meterNumber;
            private Long residentId;
            private String residentName;
            private Long billingCycleId;
            private Integer month;
            private Integer year;
            private Double previousReading;
            private Double currentReading;
            private Double unitsConsumed;
            private String anomalyStatus;
            private LocalDateTime recordedAt;

            public Builder usageId(Long usageId) { this.usageId = usageId; return this; }
            public Builder meterId(Long meterId) { this.meterId = meterId; return this; }
            public Builder meterNumber(String meterNumber) { this.meterNumber = meterNumber; return this; }
            public Builder residentId(Long residentId) { this.residentId = residentId; return this; }
            public Builder residentName(String residentName) { this.residentName = residentName; return this; }
            public Builder billingCycleId(Long billingCycleId) { this.billingCycleId = billingCycleId; return this; }
            public Builder month(Integer month) { this.month = month; return this; }
            public Builder year(Integer year) { this.year = year; return this; }
            public Builder previousReading(Double previousReading) { this.previousReading = previousReading; return this; }
            public Builder currentReading(Double currentReading) { this.currentReading = currentReading; return this; }
            public Builder unitsConsumed(Double unitsConsumed) { this.unitsConsumed = unitsConsumed; return this; }
            public Builder anomalyStatus(String anomalyStatus) { this.anomalyStatus = anomalyStatus; return this; }
            public Builder recordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; return this; }

            public UsageResponse build() {
                return new UsageResponse(usageId, meterId, meterNumber, residentId, residentName, billingCycleId, month, year, previousReading, currentReading, unitsConsumed, anomalyStatus, recordedAt);
            }
        }
    }
}
