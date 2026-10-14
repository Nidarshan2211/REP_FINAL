package com.smartwater.billing.owner.service;

import com.smartwater.billing.common.entity.BillingCycle;
import com.smartwater.billing.owner.entity.Building;
import com.smartwater.billing.owner.entity.TariffPlan;
import com.smartwater.billing.common.exception.BadRequestException;
import com.smartwater.billing.common.exception.ResourceNotFoundException;
import com.smartwater.billing.common.repository.BillingCycleRepository;
import com.smartwater.billing.owner.repository.BuildingRepository;
import com.smartwater.billing.owner.repository.TariffPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TariffPlanService {

    private final TariffPlanRepository tariffPlanRepository;
    private final BuildingRepository buildingRepository;
    private final BillingCycleRepository billingCycleRepository;

    public TariffPlanService(TariffPlanRepository tariffPlanRepository, BuildingRepository buildingRepository, BillingCycleRepository billingCycleRepository) {
        this.tariffPlanRepository = tariffPlanRepository;
        this.buildingRepository = buildingRepository;
        this.billingCycleRepository = billingCycleRepository;
    }


    @Transactional
    public TariffPlan createTariffPlan(Long buildingId, Long billingCycleId, BigDecimal baseLimit, BigDecimal baseRate, BigDecimal extraRate, BigDecimal fixedCharge, LocalDate effectiveDate) {
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + buildingId));

        BillingCycle cycle = null;
        if (billingCycleId != null) {
            cycle = billingCycleRepository.findById(billingCycleId).orElse(null);
        }

        TariffPlan plan = TariffPlan.builder()
                .building(building)
                .billingCycle(cycle)
                .baseLimit(baseLimit != null ? baseLimit : new BigDecimal("10.00"))
                .baseRate(baseRate != null ? baseRate : new BigDecimal("15.00"))
                .extraRate(extraRate != null ? extraRate : new BigDecimal("25.00"))
                .fixedCharge(fixedCharge != null ? fixedCharge : BigDecimal.ZERO)
                .effectiveFrom(effectiveDate != null ? effectiveDate : LocalDate.now())
                .effectiveDate(effectiveDate != null ? effectiveDate : LocalDate.now())
                .status(TariffPlan.PlanStatus.DRAFT)
                .build();

        return tariffPlanRepository.save(plan);
    }

    @Transactional
    public TariffPlan approveTariffPlan(Long id) {
        TariffPlan plan = tariffPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tariff plan not found with id: " + id));

        return activatePlan(plan);
    }

    @Transactional
    public TariffPlan activateTariffPlan(Long id) {
        TariffPlan plan = tariffPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tariff plan not found with id: " + id));

        return activatePlan(plan);
    }

    private TariffPlan activatePlan(TariffPlan plan) {
        Long buildingId = plan.getBuilding().getBuildingId();

        List<TariffPlan> existingPlans = tariffPlanRepository.findByBuildingBuildingIdOrderByCreatedAtDesc(buildingId);
        for (TariffPlan existing : existingPlans) {
            if (existing.getStatus() == TariffPlan.PlanStatus.ACTIVE && !existing.getId().equals(plan.getId())) {
                existing.setStatus(TariffPlan.PlanStatus.EXPIRED);
                tariffPlanRepository.save(existing);
            }
        }

        plan.setStatus(TariffPlan.PlanStatus.ACTIVE);
        return tariffPlanRepository.save(plan);
    }

    public TariffPlan getActiveTariffPlan(Long buildingId, Long cycleId) {
        if (cycleId != null) {
            return tariffPlanRepository.findByBuildingBuildingIdAndBillingCycleIdAndStatus(buildingId, cycleId, TariffPlan.PlanStatus.ACTIVE)
                    .or(() -> tariffPlanRepository.findByBillingCycleId(cycleId))
                    .or(() -> tariffPlanRepository.findFirstByBuildingBuildingIdAndStatusOrderByCreatedAtDesc(buildingId, TariffPlan.PlanStatus.ACTIVE))
                    .orElseThrow(() -> new BadRequestException("No ACTIVE TariffPlan found for building #" + buildingId));
        }

        return tariffPlanRepository.findFirstByBuildingBuildingIdAndStatusOrderByCreatedAtDesc(buildingId, TariffPlan.PlanStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException("No ACTIVE TariffPlan found for building #" + buildingId));
    }

    public List<TariffPlan> getBuildingTariffPlans(Long buildingId) {
        return tariffPlanRepository.findByBuildingBuildingIdOrderByCreatedAtDesc(buildingId);
    }

    public List<TariffPlan> getCycleTariffPlans(Long cycleId) {
        return tariffPlanRepository.findByBillingCycleId(cycleId).map(List::of).orElse(List.of());
    }

    @Transactional
    public TariffPlan createOrUpdateTariff(Long buildingId, BigDecimal baseLimit, BigDecimal baseRate, BigDecimal extraRate, LocalDate effectiveFrom) {
        TariffPlan plan = createTariffPlan(buildingId, null, baseLimit, baseRate, extraRate, BigDecimal.ZERO, effectiveFrom);
        return approveTariffPlan(plan.getId());
    }

    public TariffPlan getLatestTariff(Long buildingId) {
        try {
            return getActiveTariffPlan(buildingId, null);
        } catch (Exception e) {
            Building building = buildingRepository.findById(buildingId).orElse(null);
            return TariffPlan.builder()
                    .building(building)
                    .baseLimit(new BigDecimal("10.00"))
                    .baseRate(new BigDecimal("15.00"))
                    .extraRate(new BigDecimal("25.00"))
                    .status(TariffPlan.PlanStatus.ACTIVE)
                    .build();
        }
    }

    public List<TariffPlan> getAllTariffs(Long buildingId) {
        return getBuildingTariffPlans(buildingId);
    }
}
