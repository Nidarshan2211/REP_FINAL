package com.smartwater.billing.owner.service;

import com.smartwater.billing.owner.dto.BillingEngineDTOs.PurchaseSummaryResponse;
import com.smartwater.billing.owner.dto.BillingEngineDTOs.TariffRecommendationResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class TariffRecommendationService {

    private final WaterPurchaseCostService waterPurchaseCostService;

    public TariffRecommendationService(WaterPurchaseCostService waterPurchaseCostService) {
        this.waterPurchaseCostService = waterPurchaseCostService;
    }


    public TariffRecommendationResponse getTariffRecommendation(Long buildingId, Long billingCycleId) {
        PurchaseSummaryResponse summary = waterPurchaseCostService.calculatePurchaseSummary(buildingId, billingCycleId);

        BigDecimal avgCost = summary.getAveragePurchaseCost();
        if (avgCost == null || avgCost.compareTo(BigDecimal.ZERO) == 0) {
            avgCost = new BigDecimal("25.00"); // Fallback base supplier cost
        }

        // Apply 10% wastage factor and 15% recovery margin
        BigDecimal wastageMultiplier = new BigDecimal("1.10"); // 10% wastage
        BigDecimal marginMultiplier = new BigDecimal("1.15");  // 15% margin & maintenance

        BigDecimal baseRecommendation = avgCost.multiply(wastageMultiplier).setScale(2, RoundingMode.HALF_UP);
        BigDecimal extraRecommendation = baseRecommendation.multiply(marginMultiplier).setScale(2, RoundingMode.HALF_UP);

        TariffRecommendationResponse response = new TariffRecommendationResponse();
        response.setBuildingId(buildingId);
        response.setBillingCycleId(billingCycleId);
        response.setTotalPurchaseQuantity(summary.getTotalQuantity());
        response.setTotalPurchaseCost(summary.getTotalCost());
        response.setAveragePurchaseCost(avgCost);
        response.setEstimatedWastagePercent(new BigDecimal("10.00"));
        response.setRecommendedBaseRate(baseRecommendation);
        response.setRecommendedExtraRate(extraRecommendation);
        response.setNote("Recommended rates calculated based on average purchase cost (" + avgCost + "/kL), 10% system wastage, and overhead recovery. Building Owner review/approval required.");

        return response;
    }
}
