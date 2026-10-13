package com.smartwater.billing.repository;

import com.smartwater.billing.entity.TariffPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TariffPlanRepository extends JpaRepository<TariffPlan, Long> {
    Optional<TariffPlan> findTopByBuildingBuildingIdOrderByEffectiveFromDesc(Long buildingId);
    List<TariffPlan> findByBuildingBuildingIdOrderByEffectiveFromDesc(Long buildingId);
    List<TariffPlan> findByBuildingBuildingIdOrderByCreatedAtDesc(Long buildingId);

    Optional<TariffPlan> findByBillingCycleId(Long billingCycleId);
    Optional<TariffPlan> findByBuildingBuildingIdAndBillingCycleIdAndStatus(Long buildingId, Long billingCycleId, TariffPlan.PlanStatus status);
    Optional<TariffPlan> findFirstByBuildingBuildingIdAndStatusOrderByCreatedAtDesc(Long buildingId, TariffPlan.PlanStatus status);
}
