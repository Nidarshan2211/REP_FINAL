package com.smartwater.billing.scheduler;

import com.smartwater.billing.service.AlertEngineService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlertScheduler {

    private static final Logger log = LoggerFactory.getLogger(AlertScheduler.class);
    private final AlertEngineService alertEngineService;

    @Scheduled(cron = "0 0 8 * * *") // Runs daily at 8 AM
    public void runDailyAlertChecks() {
        log.info("AlertScheduler triggered: Running daily leak detection and over-usage audit at 8 AM...");
        alertEngineService.checkOverUsageAlerts();
        alertEngineService.runStatisticalLeakAudit();
    }
}
