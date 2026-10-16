package com.smartwater.billing.common.repository;


import com.smartwater.billing.common.entity.*;
import com.smartwater.billing.owner.entity.*;
import com.smartwater.billing.resident.entity.*;
import com.smartwater.billing.common.entity.BillingCycle;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    List<BillingCycle> findByStatus(BillingCycle.CycleStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT bc FROM BillingCycle bc WHERE bc.id = :id")
    Optional<BillingCycle> findByIdWithLock(@Param("id") Long id);
}

