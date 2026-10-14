package com.smartwater.billing.owner.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class BillingEngineDTOs {

    public static class WaterPurchaseRequest {
        @NotNull(message = "Building ID is required")
        private Long buildingId;

        private Long billingCycleId;

        @NotBlank(message = "Supplier name is required")
        private String supplier;

        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.01", message = "Quantity must be positive")
        private BigDecimal quantity;

        @NotNull(message = "Unit cost is required")
        @DecimalMin(value = "0.00", message = "Unit cost must be positive or zero")
        private BigDecimal unitCost;

        private LocalDate purchaseDate;

        public WaterPurchaseRequest() {}

        public Long getBuildingId() { return buildingId; }
        public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }

        public Long getBillingCycleId() { return billingCycleId; }
        public void setBillingCycleId(Long billingCycleId) { this.billingCycleId = billingCycleId; }

        public String getSupplier() { return supplier; }
        public void setSupplier(String supplier) { this.supplier = supplier; }

        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

        public BigDecimal getUnitCost() { return unitCost; }
        public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

        public LocalDate getPurchaseDate() { return purchaseDate; }
        public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    }

    public static class PurchaseSummaryResponse {
        private BigDecimal totalQuantity;
        private BigDecimal totalCost;
        private BigDecimal averagePurchaseCost;

        public PurchaseSummaryResponse() {}
        public PurchaseSummaryResponse(BigDecimal totalQuantity, BigDecimal totalCost, BigDecimal averagePurchaseCost) {
            this.totalQuantity = totalQuantity;
            this.totalCost = totalCost;
            this.averagePurchaseCost = averagePurchaseCost;
        }

        public BigDecimal getTotalQuantity() { return totalQuantity; }
        public void setTotalQuantity(BigDecimal totalQuantity) { this.totalQuantity = totalQuantity; }

        public BigDecimal getTotalCost() { return totalCost; }
        public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

        public BigDecimal getAveragePurchaseCost() { return averagePurchaseCost; }
        public void setAveragePurchaseCost(BigDecimal averagePurchaseCost) { this.averagePurchaseCost = averagePurchaseCost; }
    }

    public static class TariffPlanRequest {
        @NotNull(message = "Building ID is required")
        private Long buildingId;

        private Long billingCycleId;

        @NotNull(message = "Base limit is required")
        @DecimalMin(value = "0.00", message = "Base limit must be positive or zero")
        private BigDecimal baseLimit;

        @NotNull(message = "Base rate is required")
        @DecimalMin(value = "0.00", message = "Base rate must be positive or zero")
        private BigDecimal baseRate;

        @NotNull(message = "Extra rate is required")
        @DecimalMin(value = "0.00", message = "Extra rate must be positive or zero")
        private BigDecimal extraRate;

        private BigDecimal fixedCharge;
        private LocalDate effectiveDate;

        public TariffPlanRequest() {}

        public Long getBuildingId() { return buildingId; }
        public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }

        public Long getBillingCycleId() { return billingCycleId; }
        public void setBillingCycleId(Long billingCycleId) { this.billingCycleId = billingCycleId; }

        public BigDecimal getBaseLimit() { return baseLimit; }
        public void setBaseLimit(BigDecimal baseLimit) { this.baseLimit = baseLimit; }

        public BigDecimal getBaseRate() { return baseRate; }
        public void setBaseRate(BigDecimal baseRate) { this.baseRate = baseRate; }

        public BigDecimal getExtraRate() { return extraRate; }
        public void setExtraRate(BigDecimal extraRate) { this.extraRate = extraRate; }

        public BigDecimal getFixedCharge() { return fixedCharge; }
        public void setFixedCharge(BigDecimal fixedCharge) { this.fixedCharge = fixedCharge; }

        public LocalDate getEffectiveDate() { return effectiveDate; }
        public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    }

    public static class TariffRecommendationResponse {
        private Long buildingId;
        private Long billingCycleId;
        private BigDecimal totalPurchaseQuantity;
        private BigDecimal totalPurchaseCost;
        private BigDecimal averagePurchaseCost;
        private BigDecimal estimatedWastagePercent;
        private BigDecimal recommendedBaseRate;
        private BigDecimal recommendedExtraRate;
        private String note;

        public TariffRecommendationResponse() {}

        public Long getBuildingId() { return buildingId; }
        public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }

        public Long getBillingCycleId() { return billingCycleId; }
        public void setBillingCycleId(Long billingCycleId) { this.billingCycleId = billingCycleId; }

        public BigDecimal getTotalPurchaseQuantity() { return totalPurchaseQuantity; }
        public void setTotalPurchaseQuantity(BigDecimal totalPurchaseQuantity) { this.totalPurchaseQuantity = totalPurchaseQuantity; }

        public BigDecimal getTotalPurchaseCost() { return totalPurchaseCost; }
        public void setTotalPurchaseCost(BigDecimal totalPurchaseCost) { this.totalPurchaseCost = totalPurchaseCost; }

        public BigDecimal getAveragePurchaseCost() { return averagePurchaseCost; }
        public void setAveragePurchaseCost(BigDecimal averagePurchaseCost) { this.averagePurchaseCost = averagePurchaseCost; }

        public BigDecimal getEstimatedWastagePercent() { return estimatedWastagePercent; }
        public void setEstimatedWastagePercent(BigDecimal estimatedWastagePercent) { this.estimatedWastagePercent = estimatedWastagePercent; }

        public BigDecimal getRecommendedBaseRate() { return recommendedBaseRate; }
        public void setRecommendedBaseRate(BigDecimal recommendedBaseRate) { this.recommendedBaseRate = recommendedBaseRate; }

        public BigDecimal getRecommendedExtraRate() { return recommendedExtraRate; }
        public void setRecommendedExtraRate(BigDecimal recommendedExtraRate) { this.recommendedExtraRate = recommendedExtraRate; }

        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }
}
