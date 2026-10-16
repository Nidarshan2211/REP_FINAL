package com.smartwater.billing.common.ml;

import com.smartwater.billing.common.dto.LeakDTOs.LeakFeedbackRequest;
import com.smartwater.billing.common.dto.LeakDTOs.LeakFeedbackResponse;
import com.smartwater.billing.common.entity.LeakFeedback;
import com.smartwater.billing.common.entity.LeakIncident;
import com.smartwater.billing.common.entity.User;
import com.smartwater.billing.common.entity.WaterUsage;
import com.smartwater.billing.common.exception.BadRequestException;
import com.smartwater.billing.common.exception.ResourceNotFoundException;
import com.smartwater.billing.common.repository.LeakFeedbackRepository;
import com.smartwater.billing.common.repository.LeakIncidentRepository;
import com.smartwater.billing.common.repository.UserRepository;
import com.smartwater.billing.common.repository.WaterUsageRepository;
import com.smartwater.billing.owner.entity.Apartment;
import com.smartwater.billing.resident.entity.Resident;
import com.smartwater.billing.resident.repository.ResidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class LeakFeedbackService {

    private static final Logger log = LoggerFactory.getLogger(LeakFeedbackService.class);

    private static final Set<String> VALID_FEEDBACKS = Set.of(
            "REAL_LEAK_CONFIRMED",
            "CONFIRMED_LEAK",
            "FALSE_ALARM",
            "PLUMBING_WORK",
            "GUESTS_VISITING",
            "TEMPORARY_HIGH_USAGE",
            "FIXED_NOW",
            "METER_FAULT",
            "COMMON_AREA_LEAK",
            "OTHER"
    );

    private final LeakFeedbackRepository feedbackRepository;
    private final WaterUsageRepository usageRepository;
    private final UserRepository userRepository;
    private final ResidentRepository residentRepository;
    private final LeakIncidentRepository incidentRepository;

    public LeakFeedbackService(LeakFeedbackRepository feedbackRepository,
                               WaterUsageRepository usageRepository,
                               UserRepository userRepository,
                               ResidentRepository residentRepository,
                               LeakIncidentRepository incidentRepository) {
        this.feedbackRepository = feedbackRepository;
        this.usageRepository = usageRepository;
        this.userRepository = userRepository;
        this.residentRepository = residentRepository;
        this.incidentRepository = incidentRepository;
    }

    @Transactional
    public LeakFeedbackResponse submitFeedback(String userEmail, Long usageId, LeakFeedbackRequest request) {
        if (request == null || request.getFeedback() == null || request.getFeedback().isBlank()) {
            throw new BadRequestException("Feedback category is required.");
        }

        String feedbackType = request.getFeedback().trim().toUpperCase();
        if (!VALID_FEEDBACKS.contains(feedbackType)) {
            throw new BadRequestException("Invalid feedback category: " + request.getFeedback() +
                    ". Allowed values: " + VALID_FEEDBACKS);
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        WaterUsage usage = usageRepository.findById(usageId)
                .orElseThrow(() -> new ResourceNotFoundException("Water usage record not found with id: " + usageId));

        Apartment apartment = usage.getMeter() != null ? usage.getMeter().getApartment() : null;
        if (apartment == null) {
            throw new BadRequestException("Usage record #" + usageId + " is not attached to an apartment.");
        }

        // Verify authorization: Residents can only submit feedback for their own apartment
        String roleStr = user.getRole().name();
        if ("RESIDENT".equalsIgnoreCase(roleStr)) {
            Resident resident = residentRepository.findByUserEmail(userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Resident profile not found for user: " + userEmail));
            if (usage.getMeter().getResident() != null &&
                    !usage.getMeter().getResident().getResidentId().equals(resident.getResidentId())) {
                throw new BadRequestException("Residents can only submit feedback for their own apartment usage.");
            }
        }

        LeakFeedback feedback = LeakFeedback.builder()
                .usage(usage)
                .apartment(apartment)
                .submittedByUser(user)
                .submitterRole(roleStr)
                .feedback(feedbackType)
                .note(request.getNote() != null ? request.getNote().trim() : null)
                .createdAt(LocalDateTime.now())
                .build();

        LeakFeedback saved = feedbackRepository.save(feedback);
        log.info("Saved leak feedback #{} by {} for usage #{}: {}", saved.getId(), userEmail, usageId, feedbackType);

        // Update linked incident status if present
        Long incidentId = usage.getIncidentId();
        if (incidentId == null && apartment != null) {
            Optional<LeakIncident> openInc = incidentRepository.findFirstByApartmentApartmentIdAndStatusOrderByOpenedAtDesc(apartment.getApartmentId(), "OPEN");
            if (openInc.isPresent()) {
                incidentId = openInc.get().getId();
            }
        }

        if (incidentId != null) {
            incidentRepository.findById(incidentId).ifPresent(incident -> {
                if ("FALSE_ALARM".equalsIgnoreCase(feedbackType)) {
                    incident.setStatus("DISMISSED");
                    incident.setResolvedAt(LocalDateTime.now());
                } else if ("FIXED_NOW".equalsIgnoreCase(feedbackType) || "RESOLVED".equalsIgnoreCase(feedbackType)) {
                    incident.setStatus("RESOLVED");
                    incident.setResolvedAt(LocalDateTime.now());
                } else {
                    incident.setStatus("ACKNOWLEDGED");
                }
                incidentRepository.save(incident);
                log.info("Updated incident #{} status to {} upon feedback {}", incident.getId(), incident.getStatus(), feedbackType);
            });
        }

        // Export/Append to ml-service/data/feedback/leak_feedback_export.csv
        appendFeedbackToCsv(saved);

        return LeakFeedbackResponse.builder()
                .id(saved.getId())
                .usageId(usage.getUsageId())
                .apartmentId(apartment.getApartmentId())
                .submittedByUserId(user.getUserId())
                .submitterRole(roleStr)
                .feedback(saved.getFeedback())
                .note(saved.getNote())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    private void appendFeedbackToCsv(LeakFeedback fb) {
        try {
            // Check potential paths for ml-service/data/feedback
            File[] candidateDirs = new File[] {
                    new File("../ml-service/data/feedback"),
                    new File("ml-service/data/feedback"),
                    new File("../../ml-service/data/feedback")
            };

            File targetDir = null;
            for (File d : candidateDirs) {
                if (d.exists() && d.isDirectory()) {
                    targetDir = d;
                    break;
                }
            }

            if (targetDir == null) {
                targetDir = new File("../ml-service/data/feedback");
                targetDir.mkdirs();
            }

            File csvFile = new File(targetDir, "leak_feedback_export.csv");
            boolean writeHeader = !csvFile.exists() || csvFile.length() == 0;

            try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile, true))) {
                if (writeHeader) {
                    writer.println("usage_id,apartment_id,submitted_by_user_id,submitter_role,feedback,note,created_at");
                }
                String cleanNote = fb.getNote() != null ? fb.getNote().replace("\"", "\"\"") : "";
                String createdAtStr = fb.getCreatedAt() != null ?
                        fb.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "";

                writer.printf("%d,%d,%d,%s,%s,\"%s\",%s%n",
                        fb.getUsage().getUsageId(),
                        fb.getApartment().getApartmentId(),
                        fb.getSubmittedByUser().getUserId(),
                        fb.getSubmitterRole(),
                        fb.getFeedback(),
                        cleanNote,
                        createdAtStr);
            }
            log.info("Appended feedback #{} to {}", fb.getId(), csvFile.getAbsolutePath());
        } catch (Exception ex) {
            log.warn("Failed to append feedback to CSV file: {}", ex.getMessage());
        }
    }
}
