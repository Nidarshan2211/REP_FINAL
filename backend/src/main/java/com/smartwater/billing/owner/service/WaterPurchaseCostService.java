package com.smartwater.billing.owner.service;

import com.smartwater.billing.owner.dto.BillingEngineDTOs.PurchaseSummaryResponse;
import com.smartwater.billing.owner.entity.WaterPurchase;
import com.smartwater.billing.owner.repository.WaterPurchaseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class WaterPurchaseCostService {

    private final WaterPurchaseRepository waterPurchaseRepository;

    public WaterPurchaseCostService(WaterPurchaseRepository waterPurchaseRepository) {
        this.waterPurchaseRepository = waterPurchaseRepository;
    }


    public PurchaseSummaryResponse calculatePurchaseSummary(Long buildingId, Long billingCycleId) {
        List<WaterPurchase> purchases;
        if (billingCycleId != null) {
            purchases = waterPurchaseRepository.findByBillingCycleId(billingCycleId);
        } else if (buildingId != null) {
            purchases = waterPurchaseRepository.findByBuildingBuildingIdOrderByPurchaseDateDesc(buildingId);
        } else {
            purchases = List.of();
        }

        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (WaterPurchase purchase : purchases) {
            BigDecimal qty = purchase.getQuantity() != null ? purchase.getQuantity() : purchase.getVolume();
            if (qty != null) {
                totalQuantity = totalQuantity.add(qty);
            }
            if (purchase.getTotalCost() != null) {
                totalCost = totalCost.add(purchase.getTotalCost());
            }
        }

        BigDecimal averageCost = BigDecimal.ZERO;
        if (totalQuantity.compareTo(BigDecimal.ZERO) > 0) {
            averageCost = totalCost.divide(totalQuantity, 4, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
        }

        return new PurchaseSummaryResponse(
                totalQuantity.setScale(2, RoundingMode.HALF_UP),
                totalCost.setScale(2, RoundingMode.HALF_UP),
                averageCost
        );
    }
}
