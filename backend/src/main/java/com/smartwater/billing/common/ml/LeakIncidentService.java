package com.smartwater.billing.common.ml;

import com.smartwater.billing.common.entity.LeakIncident;
import com.smartwater.billing.common.entity.WaterUsage;
import com.smartwater.billing.common.repository.LeakIncidentRepository;
import com.smartwater.billing.owner.entity.Apartment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LeakIncidentService {

    private static final Logger log = LoggerFactory.getLogger(LeakIncidentService.class);
    private final LeakIncidentRepository incidentRepository;

    public LeakIncidentService(LeakIncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    /**
     * Updates or creates a multi-cycle leak incident based on current cycle's leak detection outcome.
     * Returns the linked incident id if an incident was created or updated, or null.
     */
    @Transactional
    public Long recordCycleLeakOutcome(Apartment apartment, WaterUsage usage, String anomalyStatus, Double riskScore) {
        if (apartment == null || usage == null) return null;

        Long apartmentId = apartment.getApartmentId();
        double currentScore = (riskScore != null) ? riskScore : 0.0;
        Optional<LeakIncident> openIncidentOpt = incidentRepository.findFirstByApartmentApartmentIdAndStatusOrderByOpenedAtDesc(apartmentId, "OPEN");

        if ("POTENTIAL_LEAK".equalsIgnoreCase(anomalyStatus)) {
            if (openIncidentOpt.isPresent()) {
                LeakIncident incident = openIncidentOpt.get();
                incident.setLastUsageId(usage.getUsageId());
                if (currentScore > incident.getPeakRiskScore()) {
                    incident.setPeakRiskScore(currentScore);
                }
                incidentRepository.save(incident);
                log.info("Updated existing OPEN leak incident #{} for apartment {}", incident.getId(), apartmentId);
                return incident.getId();
            } else {
                LeakIncident newIncident = LeakIncident.builder()
                        .apartment(apartment)
                        .firstUsageId(usage.getUsageId())
                        .lastUsageId(usage.getUsageId())
                        .status("OPEN")
                        .peakRiskScore(currentScore)
                        .openedAt(LocalDateTime.now())
                        .build();
                LeakIncident saved = incidentRepository.save(newIncident);
                log.info("Created new OPEN leak incident #{} for apartment {}", saved.getId(), apartmentId);
                return saved.getId();
            }
        } else if ("NORMAL".equalsIgnoreCase(anomalyStatus) && openIncidentOpt.isPresent()) {
            LeakIncident incident = openIncidentOpt.get();
            incident.setStatus("RESOLVED");
            incident.setResolvedAt(LocalDateTime.now());
            incidentRepository.save(incident);
            log.info("Auto-resolved leak incident #{} for apartment {} (usage returned to normal)", incident.getId(), apartmentId);
            return incident.getId();
        }

        return openIncidentOpt.map(LeakIncident::getId).orElse(null);
    }

    @Transactional
    public void dismissIncident(Long incidentId) {
        incidentRepository.findById(incidentId).ifPresent(incident -> {
            incident.setStatus("DISMISSED");
            incident.setResolvedAt(LocalDateTime.now());
            incidentRepository.save(incident);
            log.info("Dismissed leak incident #{}", incidentId);
        });
    }
}
