package com.smartwater.billing.common.ml;

import com.smartwater.billing.common.dto.LeakDTOs.MlFlatInputItem;
import com.smartwater.billing.common.dto.LeakDTOs.MlScoreRequest;
import com.smartwater.billing.common.dto.LeakDTOs.MlScoreResponse;
import com.smartwater.billing.common.dto.LeakDTOs.MlScoreResultItem;
import com.smartwater.billing.common.entity.BillingCycle;
import com.smartwater.billing.common.entity.WaterMeter;
import com.smartwater.billing.common.entity.WaterUsage;
import com.smartwater.billing.common.repository.BillingCycleRepository;
import com.smartwater.billing.common.repository.WaterUsageRepository;
import com.smartwater.billing.common.service.AlertEngineService;
import com.smartwater.billing.owner.entity.Apartment;
import com.smartwater.billing.owner.entity.TariffPlan;
import com.smartwater.billing.owner.repository.TariffPlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class LeakDetectionService {

    private static final Logger log = LoggerFactory.getLogger(LeakDetectionService.class);

    private final MlClient mlClient;
    private final LeakFeatureBuilder featureBuilder;
    private final LeakIncidentService incidentService;
    private final WaterUsageRepository usageRepository;
    private final BillingCycleRepository billingCycleRepository;
    private final AlertEngineService alertEngineService;
    private final TariffPlanRepository tariffPlanRepository;

    public LeakDetectionService(MlClient mlClient,
                                LeakFeatureBuilder featureBuilder,
                                LeakIncidentService incidentService,
                                WaterUsageRepository usageRepository,
                                BillingCycleRepository billingCycleRepository,
                                AlertEngineService alertEngineService,
                                TariffPlanRepository tariffPlanRepository) {
        this.mlClient = mlClient;
        this.featureBuilder = featureBuilder;
        this.incidentService = incidentService;
        this.usageRepository = usageRepository;
        this.billingCycleRepository = billingCycleRepository;
        this.alertEngineService = alertEngineService;
        this.tariffPlanRepository = tariffPlanRepository;
    }

    /**
     * Asynchronously scores a billing cycle post-commit.
     * Never delays or blocks bulk import.
     */
    @Async
    public CompletableFuture<Void> scoreBillingCycleAsync(Long buildingId, Long cycleId) {
        log.info("Triggering async leak detection scoring for building #{}, cycle #{}...", buildingId, cycleId);
        try {
            scoreBillingCycle(buildingId, cycleId);
        } catch (Exception ex) {
            log.error("Error during async leak detection scoring: {}", ex.getMessage(), ex);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Scores all water usages for the given billing cycle.
     * If the ML service fails, times out, or returns partial data, falls back gracefully to
     * statistical baseline rule (mean + 2*SD) so that usages are ALWAYS scored.
     */
    @Transactional
    public void scoreBillingCycle(Long buildingId, Long cycleId) {
        BillingCycle cycle = billingCycleRepository.findById(cycleId).orElse(null);
        if (cycle == null) {
            log.warn("Cannot score leak detection: BillingCycle #{} not found.", cycleId);
            return;
        }

        List<WaterUsage> cycleUsages = usageRepository.findByBillingCycleId(cycleId);
        if (cycleUsages.isEmpty()) {
            log.info("No usage records found for cycle #{}. Skipping scoring.", cycleId);
            return;
        }

        log.info("Scoring {} usage records for cycle #{} (Building #{})...", cycleUsages.size(), cycleId, buildingId);

        // 1. Filter out common area meters (apartment == null)
        List<WaterUsage> residentialUsages = new ArrayList<>();
        for (WaterUsage usage : cycleUsages) {
            WaterMeter meter = usage.getMeter();
            Apartment apt = (meter != null) ? meter.getApartment() : null;
            if (apt == null) {
                // Mark common-area meters as NOT_APPLICABLE
                usage.setAnomalyStatus("NOT_APPLICABLE");
                usage.setAnomalyScore(0.0);
                usage.setAnomalyReason("Common area meter - excluded from residential flat scoring.");
                usage.setAnomalyConfidence("HIGH");
                usage.setAnomalyPattern("NORMAL");
                usage.setDataQualityFlag("COMMON_AREA");
                usage.setModelVersion("system_rule");
                usage.setScoredAt(LocalDateTime.now());
            } else {
                residentialUsages.add(usage);
            }
        }

        if (residentialUsages.isEmpty()) {
            usageRepository.saveAll(cycleUsages);
            return;
        }

        // 2. Build ML request and attempt remote scoring
        MlScoreRequest request = featureBuilder.buildBatchRequest(buildingId, cycle, residentialUsages);
        Optional<MlScoreResponse> mlResponseOpt = Optional.empty();

        if (mlClient.isOperational()) {
            mlResponseOpt = mlClient.scoreBatch(request);
        }

        if (mlResponseOpt.isPresent()) {
            applyMlScoringResults(residentialUsages, mlResponseOpt.get());
        } else {
            log.warn("ML response not available. Applying statistical baseline fallback (mean + 2*SD) to {} flats.",
                    residentialUsages.size());
            applyStatisticalFallback(residentialUsages, cycleId);
        }

        // 3. Persist all updated usage records
        usageRepository.saveAll(cycleUsages);
        log.info("Successfully updated and persisted leak scoring for {} records in cycle #{}.", cycleUsages.size(), cycleId);
    }

    private void applyMlScoringResults(List<WaterUsage> residentialUsages, MlScoreResponse response) {
        Map<Long, MlScoreResultItem> resultMap = new HashMap<>();
        if (response.getResults() != null) {
            for (MlScoreResultItem item : response.getResults()) {
                if (item.getUsageId() != null) {
                    resultMap.put(item.getUsageId(), item);
                }
            }
        }

        for (WaterUsage usage : residentialUsages) {
            MlScoreResultItem res = resultMap.get(usage.getUsageId());
            if (res != null) {
                usage.setAnomalyStatus(res.getAnomalyStatus());
                usage.setAnomalyScore(res.getAnomalyScore());
                usage.setExpectedMin(res.getExpectedMin());
                usage.setExpectedMax(res.getExpectedMax());
                usage.setAnomalyReason(res.getAnomalyReason());
                usage.setAnomalyConfidence(res.getAnomalyConfidence());
                usage.setAnomalyPattern(res.getAnomalyPattern());
                usage.setDataQualityFlag(res.getDataQualityFlag());
                usage.setModelVersion(res.getModelVersion());
                usage.setScoredAt(LocalDateTime.now());

                Apartment apt = usage.getMeter() != null ? usage.getMeter().getApartment() : null;
                Long incidentId = incidentService.recordCycleLeakOutcome(apt, usage, res.getAnomalyStatus(), res.getAnomalyScore());
                usage.setIncidentId(incidentId);
            } else {
                // If single item missing from ML output, fallback individually
                applySingleStatisticalFallback(usage);
            }
        }
    }

    private void applyStatisticalFallback(List<WaterUsage> usages, Long cycleId) {
        for (WaterUsage usage : usages) {
            applySingleStatisticalFallback(usage);
        }
    }

    private void applySingleStatisticalFallback(WaterUsage usage) {
        WaterMeter meter = usage.getMeter();
        if (meter == null) return;

        double currentUnits = usage.getUnitsConsumed() != null ? usage.getUnitsConsumed() : 0.0;
        List<WaterUsage> pastLogs = usageRepository.findByMeterMeterIdOrderByYearDescMonthDesc(meter.getMeterId());

        // Exclude current usage from historical stats
        List<Double> pastUnits = new ArrayList<>();
        for (WaterUsage logItem : pastLogs) {
            if (!logItem.getUsageId().equals(usage.getUsageId()) && logItem.getUnitsConsumed() != null) {
                pastUnits.add(logItem.getUnitsConsumed());
            }
        }

        if (pastUnits.size() < 2) {
            usage.setAnomalyStatus("INSUFFICIENT_HISTORY");
            usage.setAnomalyScore(0.0);
            usage.setExpectedMin(0.0);
            usage.setExpectedMax(Math.max(15.0, currentUnits * 1.2));
            usage.setAnomalyReason("Baseline statistical rule: fewer than 2 past billing cycles available.");
            usage.setAnomalyConfidence("LOW");
            usage.setAnomalyPattern("NORMAL");
            usage.setDataQualityFlag("FALLBACK_INSUFFICIENT_HISTORY");
            usage.setModelVersion("statistical_fallback_v1");
            usage.setScoredAt(LocalDateTime.now());
            return;
        }

        double sum = 0.0;
        for (double u : pastUnits) sum += u;
        double mean = sum / pastUnits.size();

        double varianceSum = 0.0;
        for (double u : pastUnits) varianceSum += Math.pow(u - mean, 2);
        double stdDev = Math.sqrt(varianceSum / pastUnits.size());
        double leakThreshold = mean + (2 * stdDev);

        double expectedMin = Math.max(0.0, Math.round((mean - stdDev) * 100.0) / 100.0);
        double expectedMax = Math.round(leakThreshold * 100.0) / 100.0;

        String status;
        double score;
        String reason;
        String pattern;

        if (currentUnits > leakThreshold && currentUnits > 10.0) {
            status = "POTENTIAL_LEAK";
            double excessRatio = (leakThreshold > 0) ? (currentUnits - leakThreshold) / leakThreshold : 0.5;
            score = Math.min(0.99, 0.70 + Math.min(0.29, excessRatio * 0.3));
            reason = String.format(Locale.US,
                    "Usage (%.1f kL) significantly exceeds historical baseline threshold (%.1f kL). Potential leak suspected.",
                    currentUnits, leakThreshold);
            pattern = "SUDDEN_SPIKE";
        } else {
            status = "NORMAL";
            score = 0.15;
            reason = "Usage is within normal statistical thresholds based on past cycles.";
            pattern = "NORMAL";
        }

        usage.setAnomalyStatus(status);
        usage.setAnomalyScore(Math.round(score * 100.0) / 100.0);
        usage.setExpectedMin(expectedMin);
        usage.setExpectedMax(expectedMax);
        usage.setAnomalyReason(reason);
        usage.setAnomalyConfidence("MEDIUM");
        usage.setAnomalyPattern(pattern);
        usage.setDataQualityFlag("FALLBACK_STATISTICAL");
        usage.setModelVersion("statistical_fallback_v1");
        usage.setScoredAt(LocalDateTime.now());

        Apartment apt = meter.getApartment();
        Long incidentId = incidentService.recordCycleLeakOutcome(apt, usage, status, score);
        usage.setIncidentId(incidentId);
    }

    /**
     * Computes informational estimated extra water cost in Rupees if actual units exceed expected maximum.
     * Uses the cycle's actual TariffPlan tiers. Zero financial effect on actual bills.
     */
    public double calculateEstimatedExtraCost(WaterUsage usage) {
        if (usage == null || usage.getUnitsConsumed() == null || usage.getExpectedMax() == null) {
            return 0.0;
        }

        double units = usage.getUnitsConsumed();
        double expectedMax = usage.getExpectedMax();
        if (units <= expectedMax) {
            return 0.0;
        }

        TariffPlan tariff = null;
        if (usage.getBillingCycle() != null) {
            tariff = tariffPlanRepository.findByBillingCycleId(usage.getBillingCycle().getId()).orElse(null);
        }

        double actualCost = calculateTieredCharge(units, tariff);
        double maxExpectedCost = calculateTieredCharge(expectedMax, tariff);

        return Math.max(0.0, Math.round((actualCost - maxExpectedCost) * 100.0) / 100.0);
    }

    private double calculateTieredCharge(double units, TariffPlan tariff) {
        if (tariff == null) {
            return Math.round((units * 3.0 + 10.0) * 100.0) / 100.0;
        }

        BigDecimal t1Limit = tariff.getTier1Limit() != null ? tariff.getTier1Limit() : new BigDecimal("20.00");
        BigDecimal t1Rate = tariff.getTier1Rate() != null && tariff.getTier1Rate().compareTo(BigDecimal.ZERO) > 0 ? tariff.getTier1Rate() : new BigDecimal("2.50");
        BigDecimal t2Limit = tariff.getTier2Limit() != null ? tariff.getTier2Limit() : new BigDecimal("40.00");
        BigDecimal t2Rate = tariff.getTier2Rate() != null && tariff.getTier2Rate().compareTo(BigDecimal.ZERO) > 0 ? tariff.getTier2Rate() : new BigDecimal("4.00");
        BigDecimal t3Rate = tariff.getTier3Rate() != null && tariff.getTier3Rate().compareTo(BigDecimal.ZERO) > 0 ? tariff.getTier3Rate() : new BigDecimal("6.00");

        BigDecimal unitsBd = BigDecimal.valueOf(units);
        BigDecimal tier1Units = unitsBd.min(t1Limit);
        BigDecimal tier2Units = unitsBd.subtract(t1Limit).max(BigDecimal.ZERO).min(t2Limit.subtract(t1Limit));
        BigDecimal tier3Units = unitsBd.subtract(t2Limit).max(BigDecimal.ZERO);

        BigDecimal tier1Amount = tier1Units.multiply(t1Rate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tier2Amount = tier2Units.multiply(t2Rate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tier3Amount = tier3Units.multiply(t3Rate).setScale(2, RoundingMode.HALF_UP);

        return tier1Amount.add(tier2Amount).add(tier3Amount).doubleValue();
    }
}
