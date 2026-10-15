package com.smartwater.billing.owner.service;

import com.smartwater.billing.common.entity.BillingCycle;
import com.smartwater.billing.owner.entity.TariffPlan;
import com.smartwater.billing.owner.entity.WaterPurchase;
import com.smartwater.billing.common.exception.BadRequestException;
import com.smartwater.billing.common.exception.ResourceNotFoundException;
import com.smartwater.billing.common.repository.BillingCycleRepository;
import com.smartwater.billing.owner.repository.TariffPlanRepository;
import com.smartwater.billing.owner.repository.WaterPurchaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class TariffCalculationService {

    private final TariffPlanRepository tariffPlanRepository;
    private final WaterPurchaseRepository waterPurchaseRepository;
    private final BillingCycleRepository billingCycleRepository;

    public TariffCalculationService(TariffPlanRepository tariffPlanRepository, WaterPurchaseRepository waterPurchaseRepository, BillingCycleRepository billingCycleRepository) {
        this.tariffPlanRepository = tariffPlanRepository;
        this.waterPurchaseRepository = waterPurchaseRepository;
        this.billingCycleRepository = billingCycleRepository;
    }


    @Transactional
    public TariffPlan getOrGenerateTariffForCycle(BillingCycle cycle) {
        return tariffPlanRepository.findByBillingCycleId(cycle.getId())
                .orElseGet(() -> generateTariffForCycle(cycle, null));
    }

    @Transactional
    public TariffPlan generateTariffForCycle(BillingCycle cycle, BigDecimal markupOverride) {
        if (cycle.getStatus() == BillingCycle.CycleStatus.FINALIZED) {
            return tariffPlanRepository.findByBillingCycleId(cycle.getId())
                    .orElseThrow(() -> new BadRequestException("Billing cycle #" + cycle.getId() + " is FINALIZED and its tariff plan is locked against recalculation."));
        }

        List<WaterPurchase> purchases = waterPurchaseRepository.findByBillingCycleId(cycle.getId());

        BigDecimal totalVolume = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (WaterPurchase p : purchases) {
            BigDecimal qty = p.getVolume() != null ? p.getVolume() :
                             (p.getQuantity() != null ? p.getQuantity() : BigDecimal.ZERO);
            BigDecimal unitCost = p.getUnitCost() != null ? p.getUnitCost() : BigDecimal.ZERO;
            BigDecimal cost = p.getTotalCost() != null ? p.getTotalCost() : qty.multiply(unitCost);

            totalVolume = totalVolume.add(qty);
            totalCost = totalCost.add(cost);
        }

        // Zero Purchases Guard: If totalVolume == 0, averageProcurementCost = 0.00
        BigDecimal averageProcurementCost = totalVolume.compareTo(BigDecimal.ZERO) > 0
                ? totalCost.divide(totalVolume, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        TariffPlan existingPlan = tariffPlanRepository.findByBillingCycleId(cycle.getId()).orElse(null);

        BigDecimal markupPercentage = markupOverride != null ? markupOverride
                : (existingPlan != null && existingPlan.getMarkupPercentage() != null ? existingPlan.getMarkupPercentage() : new BigDecimal("20.00"));

        BigDecimal markupMultiplier = BigDecimal.ONE.add(markupPercentage.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP));
        BigDecimal baseTariff = averageProcurementCost.multiply(markupMultiplier).setScale(2, RoundingMode.HALF_UP);

        BigDecimal tier1Multiplier = existingPlan != null && existingPlan.getTier1Multiplier() != null ? existingPlan.getTier1Multiplier() : new BigDecimal("1.00");
        BigDecimal tier2Multiplier = existingPlan != null && existingPlan.getTier2Multiplier() != null ? existingPlan.getTier2Multiplier() : new BigDecimal("1.25");
        BigDecimal tier3Multiplier = existingPlan != null && existingPlan.getTier3Multiplier() != null ? existingPlan.getTier3Multiplier() : new BigDecimal("1.50");

        BigDecimal tier1Limit = new BigDecimal("20.00");
        BigDecimal tier2Limit = new BigDecimal("40.00");

        BigDecimal tier1Rate = (baseTariff.compareTo(BigDecimal.ZERO) > 0 ? baseTariff.multiply(tier1Multiplier) : new BigDecimal("2.50")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tier2Rate = (baseTariff.compareTo(BigDecimal.ZERO) > 0 ? baseTariff.multiply(tier2Multiplier) : new BigDecimal("4.00")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tier3Rate = (baseTariff.compareTo(BigDecimal.ZERO) > 0 ? baseTariff.multiply(tier3Multiplier) : new BigDecimal("6.00")).setScale(2, RoundingMode.HALF_UP);

        if (existingPlan != null) {
            existingPlan.setAverageProcurementCost(averageProcurementCost);
            existingPlan.setMarkupPercentage(markupPercentage);
            existingPlan.setBaseTariff(baseTariff);
            existingPlan.setTier1Multiplier(tier1Multiplier);
            existingPlan.setTier2Multiplier(tier2Multiplier);
            existingPlan.setTier3Multiplier(tier3Multiplier);
            existingPlan.setTier1Limit(tier1Limit);
            existingPlan.setTier1Rate(tier1Rate);
            existingPlan.setTier2Limit(tier2Limit);
            existingPlan.setTier2Rate(tier2Rate);
            existingPlan.setTier3Rate(tier3Rate);
            existingPlan.setBaseLimit(tier1Limit);
            existingPlan.setBaseRate(tier1Rate);
            existingPlan.setExtraRate(tier2Rate);
            existingPlan.setStatus(TariffPlan.PlanStatus.ACTIVE);
            return tariffPlanRepository.save(existingPlan);
        }

        TariffPlan plan = TariffPlan.builder()
                .building(cycle.getBuilding())
                .billingCycle(cycle)
                .averageProcurementCost(averageProcurementCost)
                .markupPercentage(markupPercentage)
                .baseTariff(baseTariff)
                .tier1Multiplier(tier1Multiplier)
                .tier2Multiplier(tier2Multiplier)
                .tier3Multiplier(tier3Multiplier)
                .tier1Limit(tier1Limit)
                .tier1Rate(tier1Rate)
                .tier2Limit(tier2Limit)
                .tier2Rate(tier2Rate)
                .tier3Rate(tier3Rate)
                .baseLimit(tier1Limit)
                .baseRate(tier1Rate)
                .extraRate(tier2Rate)
                .fixedCharge(BigDecimal.ZERO)
                .status(TariffPlan.PlanStatus.ACTIVE)
                .effectiveFrom(cycle.getStartDate())
                .effectiveDate(cycle.getStartDate())
                .build();

        return tariffPlanRepository.save(plan);
    }

    @Transactional
    public TariffPlan updateMarkupForCycle(Long cycleId, BigDecimal markupPercentage) {
        BillingCycle cycle = billingCycleRepository.findById(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Billing cycle not found with id: " + cycleId));

        if (cycle.getStatus() == BillingCycle.CycleStatus.FINALIZED) {
            throw new BadRequestException("Billing cycle #" + cycleId + " is FINALIZED and its markup cannot be changed.");
        }

        if (markupPercentage == null || markupPercentage.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Markup percentage must be non-negative.");
        }

        return generateTariffForCycle(cycle, markupPercentage);
    }
}
