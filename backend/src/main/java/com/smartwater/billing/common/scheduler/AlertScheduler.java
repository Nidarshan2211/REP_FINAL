package com.smartwater.billing.common.scheduler;

import com.smartwater.billing.common.service.AlertEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AlertScheduler {

    private static final Logger log = LoggerFactory.getLogger(AlertScheduler.class);
    private final AlertEngineService alertEngineService;
    private final com.smartwater.billing.common.ml.LeakDetectionService leakDetectionService;
    private final com.smartwater.billing.common.repository.BillingCycleRepository billingCycleRepository;

    public AlertScheduler(AlertEngineService alertEngineService,
                          com.smartwater.billing.common.ml.LeakDetectionService leakDetectionService,
                          com.smartwater.billing.common.repository.BillingCycleRepository billingCycleRepository) {
        this.alertEngineService = alertEngineService;
        this.leakDetectionService = leakDetectionService;
        this.billingCycleRepository = billingCycleRepository;
    }

    @Scheduled(cron = "0 0 8 * * *") // Runs daily at 8 AM
    public void runDailyAlertChecks() {
        log.info("AlertScheduler triggered: Running daily leak detection and over-usage audit at 8 AM...");
        alertEngineService.checkOverUsageAlerts();
        alertEngineService.runStatisticalLeakAudit();
    }

    @Scheduled(cron = "0 0 2 * * *") // Runs nightly at 2 AM
    public void runNightlyLeakRescoreAndRetry() {
        log.info("AlertScheduler triggered: Running nightly ML leak rescore & retry for open cycles...");
        List<com.smartwater.billing.common.entity.BillingCycle> openCycles = billingCycleRepository.findByStatus(com.smartwater.billing.common.entity.BillingCycle.CycleStatus.OPEN);
        for (com.smartwater.billing.common.entity.BillingCycle cycle : openCycles) {
            try {
                if (cycle.getBuilding() != null) {
                    leakDetectionService.scoreBillingCycle(cycle.getBuilding().getBuildingId(), cycle.getId());
                }
            } catch (Exception ex) {
                log.warn("Nightly leak rescore failed for cycle #{}: {}", cycle.getId(), ex.getMessage());
            }
        }
    }
}
