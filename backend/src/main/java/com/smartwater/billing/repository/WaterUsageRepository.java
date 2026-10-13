package com.smartwater.billing.repository;

import com.smartwater.billing.entity.WaterUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaterUsageRepository extends JpaRepository<WaterUsage, Long> {
    Optional<WaterUsage> findTopByMeterMeterIdAndMonthAndYearOrderByUsageIdDesc(Long meterId, Integer month, Integer year);
    Optional<WaterUsage> findTopByMeterMeterIdAndBillingCycleIdOrderByUsageIdDesc(Long meterId, Long billingCycleId);
    Optional<WaterUsage> findByMeterMeterIdAndBillingCycleId(Long meterId, Long billingCycleId);
    List<WaterUsage> findByBillingCycleId(Long billingCycleId);
    List<WaterUsage> findByMeterMeterIdOrderByYearDescMonthDesc(Long meterId);
    Optional<WaterUsage> findTopByMeterMeterIdOrderByRecordedAtDesc(Long meterId);
    
    @Query("SELECT u FROM WaterUsage u WHERE u.meter.resident.residentId = :residentId ORDER BY u.year DESC, u.month DESC")
    List<WaterUsage> findByResidentId(@Param("residentId") Long residentId);
    
    @Query("SELECT u FROM WaterUsage u WHERE u.meter.building.owner.ownerId = :ownerId ORDER BY u.recordedAt DESC")
    List<WaterUsage> findByOwnerId(@Param("ownerId") Long ownerId);
}
