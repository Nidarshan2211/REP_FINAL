package com.smartwater.billing.owner.entity;


import com.smartwater.billing.common.entity.BillingCycle;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tariff_plans")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TariffPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "building_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "owner"})
    private Building building;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_cycle_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "building"})
    private BillingCycle billingCycle;

    @Column(name = "average_procurement_cost", precision = 10, scale = 2)
    private BigDecimal averageProcurementCost;

    @Column(name = "markup_percentage", precision = 10, scale = 2)
    private BigDecimal markupPercentage;

    @Column(name = "base_tariff", precision = 10, scale = 2)
    private BigDecimal baseTariff;

    @Column(name = "tier1_multiplier", precision = 10, scale = 2)
    private BigDecimal tier1Multiplier;

    @Column(name = "tier2_multiplier", precision = 10, scale = 2)
    private BigDecimal tier2Multiplier;

    @Column(name = "tier3_multiplier", precision = 10, scale = 2)
    private BigDecimal tier3Multiplier;

    @Column(name = "base_limit", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseLimit;

    @Column(name = "base_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseRate;

    @Column(name = "extra_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal extraRate;

    @Column(name = "fixed_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal fixedCharge;

    @Column(name = "tier1_limit", precision = 10, scale = 2)
    private BigDecimal tier1Limit;

    @Column(name = "tier1_rate", precision = 10, scale = 2)
    private BigDecimal tier1Rate;

    @Column(name = "tier2_limit", precision = 10, scale = 2)
    private BigDecimal tier2Limit;

    @Column(name = "tier2_rate", precision = 10, scale = 2)
    private BigDecimal tier2Rate;

    @Column(name = "tier3_rate", precision = 10, scale = 2)
    private BigDecimal tier3Rate;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PlanStatus status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public TariffPlan() {}

    public TariffPlan(Long id, Building building, BillingCycle billingCycle, BigDecimal averageProcurementCost, BigDecimal markupPercentage, BigDecimal baseTariff, BigDecimal tier1Multiplier, BigDecimal tier2Multiplier, BigDecimal tier3Multiplier, BigDecimal baseLimit, BigDecimal baseRate, BigDecimal extraRate, BigDecimal fixedCharge, BigDecimal tier1Limit, BigDecimal tier1Rate, BigDecimal tier2Limit, BigDecimal tier2Rate, BigDecimal tier3Rate, LocalDate effectiveFrom, LocalDate effectiveDate, PlanStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.building = building;
        this.billingCycle = billingCycle;
        this.averageProcurementCost = averageProcurementCost;
        this.markupPercentage = markupPercentage;
        this.baseTariff = baseTariff;
        this.tier1Multiplier = tier1Multiplier;
        this.tier2Multiplier = tier2Multiplier;
        this.tier3Multiplier = tier3Multiplier;
        this.baseLimit = baseLimit;
        this.baseRate = baseRate;
        this.extraRate = extraRate;
        this.fixedCharge = fixedCharge;
        this.tier1Limit = tier1Limit;
        this.tier1Rate = tier1Rate;
        this.tier2Limit = tier2Limit;
        this.tier2Rate = tier2Rate;
        this.tier3Rate = tier3Rate;
        this.effectiveFrom = effectiveFrom;
        this.effectiveDate = effectiveDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    @PrePersist
    @PreUpdate
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (effectiveFrom == null) effectiveFrom = LocalDate.now();
        if (effectiveDate == null) effectiveDate = effectiveFrom;
        if (averageProcurementCost == null) averageProcurementCost = BigDecimal.ZERO;
        if (markupPercentage == null) markupPercentage = new BigDecimal("20.00");
        if (baseTariff == null) baseTariff = BigDecimal.ZERO;
        if (tier1Multiplier == null) tier1Multiplier = new BigDecimal("1.00");
        if (tier2Multiplier == null) tier2Multiplier = new BigDecimal("1.25");
        if (tier3Multiplier == null) tier3Multiplier = new BigDecimal("1.50");
        if (tier1Limit == null) tier1Limit = new BigDecimal("20.00");
        if (tier2Limit == null) tier2Limit = new BigDecimal("40.00");
        if (tier1Rate == null) tier1Rate = baseTariff.multiply(tier1Multiplier);
        if (tier2Rate == null) tier2Rate = baseTariff.multiply(tier2Multiplier);
        if (tier3Rate == null) tier3Rate = baseTariff.multiply(tier3Multiplier);
        if (baseLimit == null) baseLimit = tier1Limit;
        if (baseRate == null) baseRate = tier1Rate;
        if (extraRate == null) extraRate = tier2Rate;
        if (fixedCharge == null) fixedCharge = BigDecimal.ZERO;
        if (status == null) status = PlanStatus.ACTIVE;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Building getBuilding() { return building; }
    public void setBuilding(Building building) { this.building = building; }

    public BillingCycle getBillingCycle() { return billingCycle; }
    public void setBillingCycle(BillingCycle billingCycle) { this.billingCycle = billingCycle; }

    public BigDecimal getAverageProcurementCost() { return averageProcurementCost != null ? averageProcurementCost : BigDecimal.ZERO; }
    public void setAverageProcurementCost(BigDecimal averageProcurementCost) { this.averageProcurementCost = averageProcurementCost; }

    public BigDecimal getMarkupPercentage() { return markupPercentage != null ? markupPercentage : new BigDecimal("20.00"); }
    public void setMarkupPercentage(BigDecimal markupPercentage) { this.markupPercentage = markupPercentage; }

    public BigDecimal getBaseTariff() { return baseTariff != null ? baseTariff : BigDecimal.ZERO; }
    public void setBaseTariff(BigDecimal baseTariff) { this.baseTariff = baseTariff; }

    public BigDecimal getTier1Multiplier() { return tier1Multiplier != null ? tier1Multiplier : new BigDecimal("1.00"); }
    public void setTier1Multiplier(BigDecimal tier1Multiplier) { this.tier1Multiplier = tier1Multiplier; }

    public BigDecimal getTier2Multiplier() { return tier2Multiplier != null ? tier2Multiplier : new BigDecimal("1.25"); }
    public void setTier2Multiplier(BigDecimal tier2Multiplier) { this.tier2Multiplier = tier2Multiplier; }

    public BigDecimal getTier3Multiplier() { return tier3Multiplier != null ? tier3Multiplier : new BigDecimal("1.50"); }
    public void setTier3Multiplier(BigDecimal tier3Multiplier) { this.tier3Multiplier = tier3Multiplier; }

    public BigDecimal getBaseLimit() { return baseLimit != null ? baseLimit : tier1Limit; }
    public void setBaseLimit(BigDecimal baseLimit) { this.baseLimit = baseLimit; }

    public BigDecimal getBaseRate() { return baseRate != null ? baseRate : tier1Rate; }
    public void setBaseRate(BigDecimal baseRate) { this.baseRate = baseRate; }

    public BigDecimal getExtraRate() { return extraRate != null ? extraRate : tier2Rate; }
    public void setExtraRate(BigDecimal extraRate) { this.extraRate = extraRate; }

    public BigDecimal getFixedCharge() { return fixedCharge; }
    public void setFixedCharge(BigDecimal fixedCharge) { this.fixedCharge = fixedCharge; }

    public BigDecimal getTier1Limit() { return tier1Limit != null ? tier1Limit : new BigDecimal("20.00"); }
    public void setTier1Limit(BigDecimal tier1Limit) { this.tier1Limit = tier1Limit; }

    public BigDecimal getTier1Rate() { return tier1Rate != null ? tier1Rate : BigDecimal.ZERO; }
    public void setTier1Rate(BigDecimal tier1Rate) { this.tier1Rate = tier1Rate; }

    public BigDecimal getTier2Limit() { return tier2Limit != null ? tier2Limit : new BigDecimal("40.00"); }
    public void setTier2Limit(BigDecimal tier2Limit) { this.tier2Limit = tier2Limit; }

    public BigDecimal getTier2Rate() { return tier2Rate != null ? tier2Rate : BigDecimal.ZERO; }
    public void setTier2Rate(BigDecimal tier2Rate) { this.tier2Rate = tier2Rate; }

    public BigDecimal getTier3Rate() { return tier3Rate != null ? tier3Rate : BigDecimal.ZERO; }
    public void setTier3Rate(BigDecimal tier3Rate) { this.tier3Rate = tier3Rate; }

    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }

    public LocalDate getEffectiveDate() { return effectiveDate != null ? effectiveDate : effectiveFrom; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public PlanStatus getStatus() { return status; }
    public void setStatus(PlanStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Building building;
        private BillingCycle billingCycle;
        private BigDecimal averageProcurementCost;
        private BigDecimal markupPercentage;
        private BigDecimal baseTariff;
        private BigDecimal tier1Multiplier;
        private BigDecimal tier2Multiplier;
        private BigDecimal tier3Multiplier;
        private BigDecimal baseLimit;
        private BigDecimal baseRate;
        private BigDecimal extraRate;
        private BigDecimal fixedCharge;
        private BigDecimal tier1Limit;
        private BigDecimal tier1Rate;
        private BigDecimal tier2Limit;
        private BigDecimal tier2Rate;
        private BigDecimal tier3Rate;
        private LocalDate effectiveFrom;
        private LocalDate effectiveDate;
        private PlanStatus status;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder building(Building building) { this.building = building; return this; }
        public Builder billingCycle(BillingCycle billingCycle) { this.billingCycle = billingCycle; return this; }
        public Builder averageProcurementCost(BigDecimal averageProcurementCost) { this.averageProcurementCost = averageProcurementCost; return this; }
        public Builder markupPercentage(BigDecimal markupPercentage) { this.markupPercentage = markupPercentage; return this; }
        public Builder baseTariff(BigDecimal baseTariff) { this.baseTariff = baseTariff; return this; }
        public Builder tier1Multiplier(BigDecimal tier1Multiplier) { this.tier1Multiplier = tier1Multiplier; return this; }
        public Builder tier2Multiplier(BigDecimal tier2Multiplier) { this.tier2Multiplier = tier2Multiplier; return this; }
        public Builder tier3Multiplier(BigDecimal tier3Multiplier) { this.tier3Multiplier = tier3Multiplier; return this; }
        public Builder baseLimit(BigDecimal baseLimit) { this.baseLimit = baseLimit; return this; }
        public Builder baseRate(BigDecimal baseRate) { this.baseRate = baseRate; return this; }
        public Builder extraRate(BigDecimal extraRate) { this.extraRate = extraRate; return this; }
        public Builder fixedCharge(BigDecimal fixedCharge) { this.fixedCharge = fixedCharge; return this; }
        public Builder tier1Limit(BigDecimal tier1Limit) { this.tier1Limit = tier1Limit; return this; }
        public Builder tier1Rate(BigDecimal tier1Rate) { this.tier1Rate = tier1Rate; return this; }
        public Builder tier2Limit(BigDecimal tier2Limit) { this.tier2Limit = tier2Limit; return this; }
        public Builder tier2Rate(BigDecimal tier2Rate) { this.tier2Rate = tier2Rate; return this; }
        public Builder tier3Rate(BigDecimal tier3Rate) { this.tier3Rate = tier3Rate; return this; }
        public Builder effectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; return this; }
        public Builder effectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; return this; }
        public Builder status(PlanStatus status) { this.status = status; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public TariffPlan build() {
            return new TariffPlan(id, building, billingCycle, averageProcurementCost, markupPercentage, baseTariff, tier1Multiplier, tier2Multiplier, tier3Multiplier, baseLimit, baseRate, extraRate, fixedCharge, tier1Limit, tier1Rate, tier2Limit, tier2Rate, tier3Rate, effectiveFrom, effectiveDate, status, createdAt);
        }
    }

    public enum PlanStatus {
        DRAFT,
        ACTIVE,
        EXPIRED
    }
}
