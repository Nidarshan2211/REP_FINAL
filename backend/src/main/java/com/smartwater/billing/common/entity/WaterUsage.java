package com.smartwater.billing.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "water_usage")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class WaterUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usage_id")
    private Long usageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "resident"})
    private WaterMeter meter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_cycle_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "building"})
    private BillingCycle billingCycle;

    @Column(name = "`month`", nullable = false)
    private Integer month;

    @Column(name = "`year`", nullable = false)
    private Integer year;

    @Column(name = "previous_reading", nullable = false)
    private Double previousReading;

    @Column(name = "current_reading", nullable = false)
    private Double currentReading;

    @Column(name = "units_consumed", nullable = false)
    private Double unitsConsumed;

    @Column(name = "anomaly_status", length = 50)
    private String anomalyStatus;

    // ML Leak Detection & Modeling Extension Fields
    @Column(name = "anomaly_score")
    private Double anomalyScore;

    @Column(name = "expected_min")
    private Double expectedMin;

    @Column(name = "expected_max")
    private Double expectedMax;

    @Column(name = "anomaly_reason", length = 500)
    private String anomalyReason;

    @Column(name = "anomaly_confidence", length = 20)
    private String anomalyConfidence;

    @Column(name = "anomaly_pattern", length = 30)
    private String anomalyPattern;

    @Column(name = "data_quality_flag", length = 30)
    private String dataQualityFlag;

    @Column(name = "model_version", length = 30)
    private String modelVersion;

    @Column(name = "scored_at")
    private LocalDateTime scoredAt;

    @Column(name = "incident_id")
    private Long incidentId;

    @Column(name = "recorded_at", updatable = false)
    private LocalDateTime recordedAt;

    public WaterUsage() {}

    public WaterUsage(Long usageId, WaterMeter meter, BillingCycle billingCycle, Integer month, Integer year,
                      Double previousReading, Double currentReading, Double unitsConsumed, String anomalyStatus,
                      Double anomalyScore, Double expectedMin, Double expectedMax, String anomalyReason,
                      String anomalyConfidence, String anomalyPattern, String dataQualityFlag,
                      String modelVersion, LocalDateTime scoredAt, Long incidentId, LocalDateTime recordedAt) {
        this.usageId = usageId;
        this.meter = meter;
        this.billingCycle = billingCycle;
        this.month = month;
        this.year = year;
        this.previousReading = previousReading;
        this.currentReading = currentReading;
        this.unitsConsumed = unitsConsumed;
        this.anomalyStatus = anomalyStatus;
        this.anomalyScore = anomalyScore;
        this.expectedMin = expectedMin;
        this.expectedMax = expectedMax;
        this.anomalyReason = anomalyReason;
        this.anomalyConfidence = anomalyConfidence;
        this.anomalyPattern = anomalyPattern;
        this.dataQualityFlag = dataQualityFlag;
        this.modelVersion = modelVersion;
        this.scoredAt = scoredAt;
        this.incidentId = incidentId;
        this.recordedAt = recordedAt;
    }

    // Legacy constructor compatibility
    public WaterUsage(Long usageId, WaterMeter meter, BillingCycle billingCycle, Integer month, Integer year,
                      Double previousReading, Double currentReading, Double unitsConsumed, String anomalyStatus,
                      LocalDateTime recordedAt) {
        this(usageId, meter, billingCycle, month, year, previousReading, currentReading, unitsConsumed, anomalyStatus,
             null, null, null, null, null, null, null, null, null, null, recordedAt);
    }

    @PrePersist
    protected void onCreate() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
        if (unitsConsumed == null && currentReading != null && previousReading != null) {
            unitsConsumed = Math.max(0, currentReading - previousReading);
        }
        if (anomalyStatus == null) {
            anomalyStatus = "NORMAL";
        }
    }

    public Long getUsageId() { return usageId; }
    public void setUsageId(Long usageId) { this.usageId = usageId; }

    public WaterMeter getMeter() { return meter; }
    public void setMeter(WaterMeter meter) { this.meter = meter; }

    public BillingCycle getBillingCycle() { return billingCycle; }
    public void setBillingCycle(BillingCycle billingCycle) { this.billingCycle = billingCycle; }

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

    public Double getAnomalyScore() { return anomalyScore; }
    public void setAnomalyScore(Double anomalyScore) { this.anomalyScore = anomalyScore; }

    public Double getExpectedMin() { return expectedMin; }
    public void setExpectedMin(Double expectedMin) { this.expectedMin = expectedMin; }

    public Double getExpectedMax() { return expectedMax; }
    public void setExpectedMax(Double expectedMax) { this.expectedMax = expectedMax; }

    public String getAnomalyReason() { return anomalyReason; }
    public void setAnomalyReason(String anomalyReason) { this.anomalyReason = anomalyReason; }

    public String getAnomalyConfidence() { return anomalyConfidence; }
    public void setAnomalyConfidence(String anomalyConfidence) { this.anomalyConfidence = anomalyConfidence; }

    public String getAnomalyPattern() { return anomalyPattern; }
    public void setAnomalyPattern(String anomalyPattern) { this.anomalyPattern = anomalyPattern; }

    public String getDataQualityFlag() { return dataQualityFlag; }
    public void setDataQualityFlag(String dataQualityFlag) { this.dataQualityFlag = dataQualityFlag; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public LocalDateTime getScoredAt() { return scoredAt; }
    public void setScoredAt(LocalDateTime scoredAt) { this.scoredAt = scoredAt; }

    public Long getIncidentId() { return incidentId; }
    public void setIncidentId(Long incidentId) { this.incidentId = incidentId; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long usageId;
        private WaterMeter meter;
        private BillingCycle billingCycle;
        private Integer month;
        private Integer year;
        private Double previousReading;
        private Double currentReading;
        private Double unitsConsumed;
        private String anomalyStatus;
        private Double anomalyScore;
        private Double expectedMin;
        private Double expectedMax;
        private String anomalyReason;
        private String anomalyConfidence;
        private String anomalyPattern;
        private String dataQualityFlag;
        private String modelVersion;
        private LocalDateTime scoredAt;
        private Long incidentId;
        private LocalDateTime recordedAt;

        public Builder usageId(Long usageId) { this.usageId = usageId; return this; }
        public Builder meter(WaterMeter meter) { this.meter = meter; return this; }
        public Builder billingCycle(BillingCycle billingCycle) { this.billingCycle = billingCycle; return this; }
        public Builder month(Integer month) { this.month = month; return this; }
        public Builder year(Integer year) { this.year = year; return this; }
        public Builder previousReading(Double previousReading) { this.previousReading = previousReading; return this; }
        public Builder currentReading(Double currentReading) { this.currentReading = currentReading; return this; }
        public Builder unitsConsumed(Double unitsConsumed) { this.unitsConsumed = unitsConsumed; return this; }
        public Builder anomalyStatus(String anomalyStatus) { this.anomalyStatus = anomalyStatus; return this; }
        public Builder anomalyScore(Double anomalyScore) { this.anomalyScore = anomalyScore; return this; }
        public Builder expectedMin(Double expectedMin) { this.expectedMin = expectedMin; return this; }
        public Builder expectedMax(Double expectedMax) { this.expectedMax = expectedMax; return this; }
        public Builder anomalyReason(String anomalyReason) { this.anomalyReason = anomalyReason; return this; }
        public Builder anomalyConfidence(String anomalyConfidence) { this.anomalyConfidence = anomalyConfidence; return this; }
        public Builder anomalyPattern(String anomalyPattern) { this.anomalyPattern = anomalyPattern; return this; }
        public Builder dataQualityFlag(String dataQualityFlag) { this.dataQualityFlag = dataQualityFlag; return this; }
        public Builder modelVersion(String modelVersion) { this.modelVersion = modelVersion; return this; }
        public Builder scoredAt(LocalDateTime scoredAt) { this.scoredAt = scoredAt; return this; }
        public Builder incidentId(Long incidentId) { this.incidentId = incidentId; return this; }
        public Builder recordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; return this; }

        public WaterUsage build() {
            return new WaterUsage(usageId, meter, billingCycle, month, year, previousReading, currentReading,
                    unitsConsumed, anomalyStatus, anomalyScore, expectedMin, expectedMax, anomalyReason,
                    anomalyConfidence, anomalyPattern, dataQualityFlag, modelVersion, scoredAt, incidentId, recordedAt);
        }
    }
}
