package com.smartwater.billing.repository;

import com.smartwater.billing.entity.BillingCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillingCycleRepository extends JpaRepository<BillingCycle, Long> {
    List<BillingCycle> findByBuildingBuildingIdOrderByStartDateDesc(Long buildingId);
    Optional<BillingCycle> findTopByBuildingBuildingIdAndStatus(Long buildingId, BillingCycle.CycleStatus status);
    Optional<BillingCycle> findByBuildingBuildingIdAndBillingMonth(Long buildingId, LocalDate billingMonth);
    Optional<BillingCycle> findByBuildingBuildingIdAndStartDateAndEndDate(Long buildingId, LocalDate startDate, LocalDate endDate);
}
