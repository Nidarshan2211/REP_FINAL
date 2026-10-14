package com.smartwater.billing.owner.repository;


import com.smartwater.billing.common.entity.*;
import com.smartwater.billing.owner.entity.*;
import com.smartwater.billing.resident.entity.*;
import com.smartwater.billing.owner.entity.WaterPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WaterPurchaseRepository extends JpaRepository<WaterPurchase, Long> {
    List<WaterPurchase> findByBuildingBuildingIdOrderByPurchaseDateDesc(Long buildingId);
    List<WaterPurchase> findByBillingCycleId(Long billingCycleId);
}
