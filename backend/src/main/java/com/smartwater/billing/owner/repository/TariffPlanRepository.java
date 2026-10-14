package com.smartwater.billing.owner.repository;


import com.smartwater.billing.common.entity.*;
import com.smartwater.billing.owner.entity.*;
import com.smartwater.billing.resident.entity.*;
import com.smartwater.billing.owner.entity.TariffPlan;
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
