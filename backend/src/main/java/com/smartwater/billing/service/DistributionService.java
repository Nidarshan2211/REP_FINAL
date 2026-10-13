package com.smartwater.billing.service;

import com.smartwater.billing.entity.Resident;
import com.smartwater.billing.entity.WaterMeter;
import com.smartwater.billing.entity.WaterPurchase;
import com.smartwater.billing.entity.WaterUsage;
import com.smartwater.billing.repository.ResidentRepository;
import com.smartwater.billing.repository.WaterMeterRepository;
import com.smartwater.billing.repository.WaterPurchaseRepository;
import com.smartwater.billing.repository.WaterUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DistributionService {

    private final WaterPurchaseRepository waterPurchaseRepository;
    private final ResidentRepository residentRepository;
    private final WaterMeterRepository waterMeterRepository;
    private final WaterUsageRepository waterUsageRepository;

    public Map<String, BigDecimal> calculateCost(BigDecimal unitsConsumed, BigDecimal baseLimit, BigDecimal baseRate, BigDecimal extraRate) {
        if (unitsConsumed == null) unitsConsumed = BigDecimal.ZERO;
        if (baseLimit == null) baseLimit = new BigDecimal("10.00");
        if (baseRate == null) baseRate = new BigDecimal("15.00");
        if (extraRate == null) extraRate = new BigDecimal("25.00");

        BigDecimal baseCharge;
        BigDecimal extraCharge;

        if (unitsConsumed.compareTo(baseLimit) <= 0) {
            baseCharge = unitsConsumed.multiply(baseRate);
            extraCharge = BigDecimal.ZERO;
        } else {
            baseCharge = baseLimit.multiply(baseRate);
            BigDecimal extraUnits = unitsConsumed.subtract(baseLimit);
            extraCharge = extraUnits.multiply(extraRate);
        }

        Map<String, BigDecimal> result = new HashMap<>();
        result.put("baseCharge", baseCharge.setScale(2, RoundingMode.HALF_UP));
        result.put("extraCharge", extraCharge.setScale(2, RoundingMode.HALF_UP));
        result.put("totalSlabCost", baseCharge.add(extraCharge).setScale(2, RoundingMode.HALF_UP));
        return result;
    }

    public Map<Long, BigDecimal> calculateByUsage(Long buildingId, Long cycleId) {
        List<WaterPurchase> purchases = cycleId != null ?
                waterPurchaseRepository.findByBillingCycleId(cycleId) :
                waterPurchaseRepository.findByBuildingBuildingIdOrderByPurchaseDateDesc(buildingId);

        BigDecimal totalBulkCost = purchases.stream()
                .map(WaterPurchase::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Resident> residents = residentRepository.findByBuildingOwnerOwnerId(buildingId);
        if (residents.isEmpty()) {
            residents = residentRepository.findAll();
        }

        BigDecimal totalConsumption = BigDecimal.ZERO;
        Map<Long, BigDecimal> residentUsageMap = new HashMap<>();

        for (Resident r : residents) {
            WaterMeter meter = waterMeterRepository.findByResidentResidentId(r.getResidentId()).orElse(null);
            BigDecimal usage = BigDecimal.ZERO;
            if (meter != null) {
                List<WaterUsage> logs = waterUsageRepository.findByMeterMeterIdOrderByYearDescMonthDesc(meter.getMeterId());
                if (!logs.isEmpty()) {
                    usage = BigDecimal.valueOf(logs.get(0).getUnitsConsumed());
                }
            }
            residentUsageMap.put(r.getResidentId(), usage);
            totalConsumption = totalConsumption.add(usage);
        }

        Map<Long, BigDecimal> distributionMap = new HashMap<>();
        for (Resident r : residents) {
            BigDecimal usage = residentUsageMap.getOrDefault(r.getResidentId(), BigDecimal.ZERO);
            BigDecimal share;
            if (totalConsumption.compareTo(BigDecimal.ZERO) > 0) {
                share = usage.divide(totalConsumption, 4, RoundingMode.HALF_UP).multiply(totalBulkCost);
            } else if (!residents.isEmpty()) {
                share = totalBulkCost.divide(BigDecimal.valueOf(residents.size()), 2, RoundingMode.HALF_UP);
            } else {
                share = BigDecimal.ZERO;
            }
            distributionMap.put(r.getResidentId(), share.setScale(2, RoundingMode.HALF_UP));
        }

        return distributionMap;
    }

    public Map<Long, BigDecimal> calculateByArea(Long buildingId, Long cycleId) {
        List<WaterPurchase> purchases = cycleId != null ?
                waterPurchaseRepository.findByBillingCycleId(cycleId) :
                waterPurchaseRepository.findByBuildingBuildingIdOrderByPurchaseDateDesc(buildingId);

        BigDecimal totalBulkCost = purchases.stream()
                .map(WaterPurchase::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Resident> residents = residentRepository.findByBuildingOwnerOwnerId(buildingId);
        if (residents.isEmpty()) {
            residents = residentRepository.findAll();
        }

        Map<Long, BigDecimal> distributionMap = new HashMap<>();
        if (residents.isEmpty()) return distributionMap;

        BigDecimal equalShare = totalBulkCost.divide(BigDecimal.valueOf(residents.size()), 2, RoundingMode.HALF_UP);
        for (Resident r : residents) {
            distributionMap.put(r.getResidentId(), equalShare);
        }

        return distributionMap;
    }
}
