package com.smartwater.billing.repository;

import com.smartwater.billing.entity.WaterMeter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaterMeterRepository extends JpaRepository<WaterMeter, Long> {
    Optional<WaterMeter> findByMeterNumber(String meterNumber);
    Optional<WaterMeter> findFirstByResidentResidentId(Long residentId);
    Optional<WaterMeter> findByResidentResidentId(Long residentId);
    List<WaterMeter> findByBuildingBuildingId(Long buildingId);
    List<WaterMeter> findByBuildingOwnerOwnerId(Long ownerId);
}
