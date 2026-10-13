package com.smartwater.billing.service;

import com.smartwater.billing.entity.*;
import com.smartwater.billing.repository.NotificationRepository;
import com.smartwater.billing.repository.ResidentRepository;
import com.smartwater.billing.repository.WaterMeterRepository;
import com.smartwater.billing.repository.WaterUsageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertEngineService {

    private static final Logger log = LoggerFactory.getLogger(AlertEngineService.class);

    private final WaterMeterRepository waterMeterRepository;
    private final WaterUsageRepository waterUsageRepository;
    private final NotificationRepository notificationRepository;
    private final ResidentRepository residentRepository;
    private final EmailService emailService;

    public String evaluateLeakStatus(Long meterId, Double currentUnits) {
        List<WaterUsage> logs = waterUsageRepository.findByMeterMeterIdOrderByYearDescMonthDesc(meterId);
        if (logs.size() < 2) {
            return "INSUFFICIENT_HISTORY";
        }

        double sum = 0.0;
        for (WaterUsage logItem : logs) {
            sum += logItem.getUnitsConsumed();
        }
        double mean = sum / logs.size();

        double varianceSum = 0.0;
        for (WaterUsage logItem : logs) {
            varianceSum += Math.pow(logItem.getUnitsConsumed() - mean, 2);
        }
        double stdDev = Math.sqrt(varianceSum / logs.size());
        double leakThreshold = mean + (2 * stdDev);

        if (currentUnits > leakThreshold && currentUnits > 10.0) {
            return "POTENTIAL_LEAK";
        }
        return "NORMAL";
    }

    public void checkOverUsageAlerts() {
        log.info("Running daily Over-Usage Alert Audit...");
        List<Resident> residents = residentRepository.findAll();
        for (Resident resident : residents) {
            WaterMeter meter = waterMeterRepository.findByResidentResidentId(resident.getResidentId()).orElse(null);
            if (meter != null) {
                List<WaterUsage> usageList = waterUsageRepository.findByMeterMeterIdOrderByYearDescMonthDesc(meter.getMeterId());
                if (!usageList.isEmpty()) {
                    double currentUnits = usageList.get(0).getUnitsConsumed();
                    double monthlyLimit = 20.0; // kL threshold

                    if (currentUnits > monthlyLimit) {
                        double excess = Math.round((currentUnits - monthlyLimit) * 100.0) / 100.0;
                        String title = "⚠️ Over Usage Warning!";

                        // Duplicate Alert Protection: Check if warning was sent in the last 25 days
                        List<Notification> existingAlerts = notificationRepository.findByResidentResidentIdOrderByCreatedAtDesc(resident.getResidentId());
                        boolean duplicateExists = existingAlerts.stream().anyMatch(n -> 
                            title.equalsIgnoreCase(n.getTitle()) && 
                            n.getCreatedAt() != null && 
                            n.getCreatedAt().isAfter(LocalDateTime.now().minusDays(25))
                        );

                        if (duplicateExists) {
                            log.info("Overusage alert already sent to resident {} for this cycle. Skipping duplicate.", resident.getName());
                            continue;
                        }

                        String message = String.format(
                                "Your water consumption is %.2f kL, which is above the recommended %.2f kL monthly threshold (Excess: %.2f kL).\n\n" +
                                "Try:\n" +
                                "• Fixing leaks\n" +
                                "• Reducing unnecessary water usage\n" +
                                "• Monitoring daily consumption",
                                currentUnits, monthlyLimit, excess
                        );

                        Notification alert = Notification.builder()
                                .resident(resident)
                                .title(title)
                                .message(message)
                                .isRead(false)
                                .build();
                        notificationRepository.save(alert);

                        emailService.sendNotificationEmail(resident.getEmail(), resident.getName(), title, message);
                    }
                }
            }
        }
    }

    public void runStatisticalLeakAudit() {
        log.info("Running Statistical Leak Audit (Mean + 2 * StdDev)...");
        List<WaterMeter> meters = waterMeterRepository.findAll();
        for (WaterMeter m : meters) {
            List<WaterUsage> logs = waterUsageRepository.findByMeterMeterIdOrderByYearDescMonthDesc(m.getMeterId());
            if (logs.size() < 2) continue;

            double latestUsage = logs.get(0).getUnitsConsumed();
            
            double sum = 0.0;
            for (WaterUsage logItem : logs) sum += logItem.getUnitsConsumed();
            double mean = Math.round((sum / logs.size()) * 100.0) / 100.0;

            double varianceSum = 0.0;
            for (WaterUsage logItem : logs) varianceSum += Math.pow(logItem.getUnitsConsumed() - mean, 2);
            double stdDev = Math.sqrt(varianceSum / logs.size());
            double leakThreshold = Math.round((mean + (2 * stdDev)) * 100.0) / 100.0;

            String status = evaluateLeakStatus(m.getMeterId(), latestUsage);

            if ("POTENTIAL_LEAK".equals(status)) {
                Resident resident = m.getResident();
                if (resident != null && resident.getBuilding() != null && resident.getBuilding().getOwner() != null) {
                    BuildingOwner owner = resident.getBuilding().getOwner();
                    String ownerEmail = owner.getUser() != null ? owner.getUser().getEmail() : owner.getPhone();

                    String title = "🚨 Water Usage Anomaly Detected";
                    
                    // Duplicate Alert Protection
                    List<Notification> existingAlerts = notificationRepository.findByResidentResidentIdOrderByCreatedAtDesc(resident.getResidentId());
                    boolean duplicateExists = existingAlerts.stream().anyMatch(n -> 
                        title.equalsIgnoreCase(n.getTitle()) && 
                        n.getCreatedAt() != null && 
                        n.getCreatedAt().isAfter(LocalDateTime.now().minusDays(25))
                    );

                    if (duplicateExists) {
                        log.info("Anomaly report already logged for meter {}. Skipping duplicate notification.", m.getMeterNumber());
                        continue;
                    }

                    String reportMessage = String.format(
                            "Water Usage Anomaly Detected\n\n" +
                            "Apartment: %s\n" +
                            "Resident: %s\n" +
                            "Meter: %s\n" +
                            "Current Usage: %.2f kL\n" +
                            "Historical Average: %.2f kL\n" +
                            "Detection Threshold: %.2f kL\n\n" +
                            "Status: POTENTIAL_LEAK",
                            resident.getApartmentNumber(), resident.getName(), m.getMeterNumber(), latestUsage, mean, leakThreshold
                    );

                    // Notify Resident
                    Notification residentAlert = Notification.builder()
                            .resident(resident)
                            .title(title)
                            .message("Warning! Unusual high water consumption detected on your meter (" + latestUsage + " kL). Potential leak identified.")
                            .isRead(false)
                            .build();
                    notificationRepository.save(residentAlert);

                    // Email Building Owner / Admin
                    if (ownerEmail != null && !ownerEmail.isEmpty()) {
                        emailService.sendNotificationEmail(ownerEmail, owner.getName(), title, reportMessage);
                    }
                }
            }
        }
    }
}
