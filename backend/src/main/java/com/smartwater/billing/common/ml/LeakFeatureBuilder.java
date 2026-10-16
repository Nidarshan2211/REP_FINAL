package com.smartwater.billing.common.ml;

import com.smartwater.billing.common.dto.LeakDTOs.MlFlatInputItem;
import com.smartwater.billing.common.dto.LeakDTOs.MlScoreRequest;
import com.smartwater.billing.common.entity.BillingCycle;
import com.smartwater.billing.common.entity.WaterMeter;
import com.smartwater.billing.common.entity.WaterUsage;
import com.smartwater.billing.common.repository.WaterUsageRepository;
import com.smartwater.billing.owner.entity.Apartment;
import com.smartwater.billing.resident.entity.ApartmentOccupancy;
import com.smartwater.billing.resident.entity.ApartmentOccupancy.OccupancyStatus;
import com.smartwater.billing.resident.entity.AwayPeriod;
import com.smartwater.billing.resident.repository.ApartmentOccupancyRepository;
import com.smartwater.billing.resident.repository.AwayPeriodRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class LeakFeatureBuilder {

    private static final Logger log = LoggerFactory.getLogger(LeakFeatureBuilder.class);

    private final WaterUsageRepository usageRepository;
    private final ApartmentOccupancyRepository occupancyRepository;
    private final AwayPeriodRepository awayPeriodRepository;
    private final HouseholdProperties householdProperties;

    public LeakFeatureBuilder(WaterUsageRepository usageRepository,
                              ApartmentOccupancyRepository occupancyRepository,
                              AwayPeriodRepository awayPeriodRepository,
                              HouseholdProperties householdProperties) {
        this.usageRepository = usageRepository;
        this.occupancyRepository = occupancyRepository;
        this.awayPeriodRepository = awayPeriodRepository;
        this.householdProperties = householdProperties;
    }

    /**
     * Builds the complete anonymous ML scoring request for all residential flat usages in a billing cycle.
     * Common area meters (apartment_id is null) are excluded and handled separately.
     */
    public MlScoreRequest buildBatchRequest(Long buildingId, BillingCycle cycle, List<WaterUsage> cycleUsages) {
        String cycleIdStr = cycle != null ? "CYC-" + cycle.getId() : "CYC-" + System.currentTimeMillis();
        LocalDate cycleStart = (cycle != null && cycle.getStartDate() != null) ? cycle.getStartDate() : LocalDate.now().minusDays(30);
        LocalDate cycleEnd = (cycle != null && cycle.getEndDate() != null) ? cycle.getEndDate() : LocalDate.now();
        int cycleDays = (int) ChronoUnit.DAYS.between(cycleStart, cycleEnd) + 1;
        if (cycleDays <= 0) cycleDays = 30;

        int billingMonth = cycleStart.getMonthValue();
        int billingYear = cycleStart.getYear();

        // 1. Separate residential flat usages from common area / unassigned meters
        Map<WaterUsage, Apartment> residentialUsageMap = new LinkedHashMap<>();
        List<Double> allFlatUnits = new ArrayList<>();

        for (WaterUsage u : cycleUsages) {
            WaterMeter meter = u.getMeter();
            Apartment apt = (meter != null) ? meter.getApartment() : null;
            if (apt != null && apt.getApartmentId() != null) {
                residentialUsageMap.put(u, apt);
                allFlatUnits.add(u.getUnitsConsumed() != null ? u.getUnitsConsumed() : 0.0);
            }
        }

        // 2. Compute building-level peer statistics (Median and P90)
        double peerMedian = computePercentile(allFlatUnits, 0.50);
        double peerP90 = computePercentile(allFlatUnits, 0.90);

        // 3. Build anonymous flat feature items
        List<MlFlatInputItem> items = new ArrayList<>();

        for (Map.Entry<WaterUsage, Apartment> entry : residentialUsageMap.entrySet()) {
            WaterUsage usage = entry.getKey();
            Apartment apt = entry.getValue();
            Long apartmentId = apt.getApartmentId();

            // Resolve active occupancy & household size
            Optional<ApartmentOccupancy> activeOcc = occupancyRepository.findFirstByApartmentApartmentIdAndStatus(apartmentId, OccupancyStatus.ACTIVE);
            int householdSize;
            int childrenCount = 0;

            if (activeOcc.isPresent() && activeOcc.get().getHouseholdSize() != null && activeOcc.get().getHouseholdSize() > 0) {
                householdSize = activeOcc.get().getHouseholdSize();
                childrenCount = activeOcc.get().getChildrenCount() != null ? activeOcc.get().getChildrenCount() : 0;
            } else {
                householdSize = householdProperties.getDefaultSizeForUnitType(apt.getUnitType());
            }

            // Calculate away days overlapping current cycle
            int awayDaysInCycle = calculateAwayDays(apartmentId, cycleStart, cycleEnd);

            // Fetch historical units (excluding current usage)
            List<Double> historicalUnits = fetchHistoricalUnits(usage.getMeter().getMeterId(), cycle != null ? cycle.getId() : null);

            MlFlatInputItem item = MlFlatInputItem.builder()
                    .apartmentId(apartmentId)
                    .usageId(usage.getUsageId())
                    .unitsConsumed(usage.getUnitsConsumed())
                    .householdSize(householdSize)
                    .childrenCount(childrenCount)
                    .unitType(apt.getUnitType() != null ? apt.getUnitType() : "2BHK")
                    .floorNumber(apt.getFloorNumber() != null ? apt.getFloorNumber() : 1)
                    .awayDaysInCycle(awayDaysInCycle)
                    .cycleDays(cycleDays)
                    .billingMonth(usage.getMonth() != null ? usage.getMonth() : billingMonth)
                    .billingYear(usage.getYear() != null ? usage.getYear() : billingYear)
                    .historicalUnits(historicalUnits)
                    .peerMedianUnits(peerMedian)
                    .peerP90Units(peerP90)
                    .build();

            items.add(item);
        }

        return MlScoreRequest.builder()
                .cycleId(cycleIdStr)
                .buildingId(buildingId)
                .items(items)
                .build();
    }

    private int calculateAwayDays(Long apartmentId, LocalDate cycleStart, LocalDate cycleEnd) {
        try {
            List<AwayPeriod> periods = awayPeriodRepository.findByApartmentAndDateRange(apartmentId, cycleStart, cycleEnd);
            int totalAwayDays = 0;
            for (AwayPeriod p : periods) {
                LocalDate start = p.getStartDate().isBefore(cycleStart) ? cycleStart : p.getStartDate();
                LocalDate end = p.getEndDate().isAfter(cycleEnd) ? cycleEnd : p.getEndDate();
                if (!end.isBefore(start)) {
                    totalAwayDays += (int) ChronoUnit.DAYS.between(start, end) + 1;
                }
            }
            return Math.min(totalAwayDays, (int) ChronoUnit.DAYS.between(cycleStart, cycleEnd) + 1);
        } catch (Exception ex) {
            log.warn("Error calculating away days for apartment {}: {}", apartmentId, ex.getMessage());
            return 0;
        }
    }

    private List<Double> fetchHistoricalUnits(Long meterId, Long currentCycleId) {
        if (meterId == null) return Collections.emptyList();
        List<WaterUsage> historical = usageRepository.findByMeterMeterIdOrderByYearDescMonthDesc(meterId);
        return historical.stream()
                .filter(u -> currentCycleId == null || u.getBillingCycle() == null || !currentCycleId.equals(u.getBillingCycle().getId()))
                .map(WaterUsage::getUnitsConsumed)
                .filter(Objects::nonNull)
                .limit(12)
                .collect(Collectors.toList());
    }

    private double computePercentile(List<Double> values, double percentile) {
        if (values == null || values.isEmpty()) return 10.0;
        List<Double> sorted = values.stream().filter(Objects::nonNull).sorted().collect(Collectors.toList());
        if (sorted.isEmpty()) return 10.0;
        if (sorted.size() == 1) return sorted.get(0);

        double index = percentile * (sorted.size() - 1);
        int lower = (int) Math.floor(index);
        int upper = (int) Math.ceil(index);
        if (lower == upper) {
            return sorted.get(lower);
        }
        double weight = index - lower;
        return sorted.get(lower) * (1.0 - weight) + sorted.get(upper) * weight;
    }
}
