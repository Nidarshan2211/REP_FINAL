package com.smartwater.billing.repository;

import com.smartwater.billing.entity.WaterPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WaterPurchaseRepository extends JpaRepository<WaterPurchase, Long> {
    List<WaterPurchase> findByBuildingBuildingIdOrderByPurchaseDateDesc(Long buildingId);
    List<WaterPurchase> findByBillingCycleId(Long billingCycleId);
}
