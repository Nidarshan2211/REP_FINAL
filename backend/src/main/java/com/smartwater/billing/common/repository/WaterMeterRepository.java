package com.smartwater.billing.common.repository;


import com.smartwater.billing.common.entity.*;
import com.smartwater.billing.owner.entity.*;
import com.smartwater.billing.resident.entity.*;
import com.smartwater.billing.common.entity.WaterMeter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaterMeterRepository extends JpaRepository<WaterMeter, Long> {
    Optional<WaterMeter> findByMeterNumber(String meterNumber);
    Optional<WaterMeter> findFirstByResidentResidentId(Long residentId);
    Optional<WaterMeter> findByResidentResidentId(Long residentId);
    Optional<WaterMeter> findFirstByApartmentApartmentId(Long apartmentId);
    Optional<WaterMeter> findByApartmentApartmentId(Long apartmentId);
    List<WaterMeter> findByBuildingBuildingId(Long buildingId);
    List<WaterMeter> findByBuildingOwnerOwnerId(Long ownerId);
}
