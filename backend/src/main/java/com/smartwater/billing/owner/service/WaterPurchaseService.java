package com.smartwater.billing.owner.service;

import com.smartwater.billing.common.entity.BillingCycle;
import com.smartwater.billing.owner.entity.Building;
import com.smartwater.billing.owner.entity.WaterPurchase;
import com.smartwater.billing.common.exception.BadRequestException;
import com.smartwater.billing.common.exception.ResourceNotFoundException;
import com.smartwater.billing.common.repository.BillingCycleRepository;
import com.smartwater.billing.owner.repository.BuildingRepository;
import com.smartwater.billing.owner.repository.WaterPurchaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class WaterPurchaseService {

    private final WaterPurchaseRepository waterPurchaseRepository;
    private final BuildingRepository buildingRepository;
    private final BillingCycleRepository billingCycleRepository;
    private final TariffCalculationService tariffCalculationService;

    public WaterPurchaseService(WaterPurchaseRepository waterPurchaseRepository, BuildingRepository buildingRepository, BillingCycleRepository billingCycleRepository, TariffCalculationService tariffCalculationService) {
        this.waterPurchaseRepository = waterPurchaseRepository;
        this.buildingRepository = buildingRepository;
        this.billingCycleRepository = billingCycleRepository;
        this.tariffCalculationService = tariffCalculationService;
    }


    @Transactional
    public WaterPurchase recordPurchase(Long buildingId, Long cycleId, String supplier, BigDecimal volume, BigDecimal unitCost, LocalDate purchaseDate) {
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + buildingId));

        BillingCycle cycle = null;
        if (cycleId != null) {
            cycle = billingCycleRepository.findById(cycleId).orElse(null);
        }

        if (cycle != null && cycle.getStatus() == BillingCycle.CycleStatus.FINALIZED) {
            throw new BadRequestException("Billing cycle #" + cycle.getId() + " is FINALIZED and locked. Bulk purchases cannot be added.");
        }

        BigDecimal totalCost = volume.multiply(unitCost);

        WaterPurchase purchase = WaterPurchase.builder()
                .building(building)
                .billingCycle(cycle)
                .supplier(supplier)
                .volume(volume)
                .unitCost(unitCost)
                .totalCost(totalCost)
                .purchaseDate(purchaseDate != null ? purchaseDate : LocalDate.now())
                .build();

        WaterPurchase savedPurchase = waterPurchaseRepository.save(purchase);

        // Recalculate tariff dynamically for open cycle
        if (cycle != null && cycle.getStatus() == BillingCycle.CycleStatus.OPEN) {
            tariffCalculationService.generateTariffForCycle(cycle, null);
        }

        return savedPurchase;
    }

    public List<WaterPurchase> getPurchases(Long buildingId) {
        return waterPurchaseRepository.findByBuildingBuildingIdOrderByPurchaseDateDesc(buildingId);
    }

    public List<WaterPurchase> getPurchasesByCycle(Long cycleId) {
        return waterPurchaseRepository.findByBillingCycleId(cycleId);
    }

    @Transactional
    public void deletePurchase(Long id) {
        WaterPurchase purchase = waterPurchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Water purchase not found with id: " + id));

        BillingCycle cycle = purchase.getBillingCycle();
        if (cycle != null && cycle.getStatus() == BillingCycle.CycleStatus.FINALIZED) {
            throw new BadRequestException("Billing cycle #" + cycle.getId() + " is FINALIZED and locked. Bulk purchases cannot be deleted.");
        }

        waterPurchaseRepository.delete(purchase);

        // Recalculate tariff dynamically from remaining purchase records
        if (cycle != null && cycle.getStatus() == BillingCycle.CycleStatus.OPEN) {
            tariffCalculationService.generateTariffForCycle(cycle, null);
        }
    }
}
