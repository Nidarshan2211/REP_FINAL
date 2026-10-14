package com.smartwater.billing.resident.controller;

import com.smartwater.billing.common.dto.AuthDTOs.MessageResponse;
import com.smartwater.billing.resident.entity.Notification;
import com.smartwater.billing.resident.entity.Resident;
import com.smartwater.billing.resident.repository.NotificationRepository;
import com.smartwater.billing.resident.repository.ResidentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/notifications", "/api/notifications"})
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final ResidentRepository residentRepository;

    public NotificationController(NotificationRepository notificationRepository, ResidentRepository residentRepository) {
        this.notificationRepository = notificationRepository;
        this.residentRepository = residentRepository;
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER', 'RESIDENT')")
    public ResponseEntity<List<Notification>> getNotifications(Authentication authentication) {
        Resident resident = residentRepository.findByUserEmail(authentication.getName())
                .or(() -> residentRepository.findByEmail(authentication.getName()))
                .orElse(null);

        if (resident == null) {
            return ResponseEntity.ok(List.of());
        }

        return ResponseEntity.ok(notificationRepository.findByResidentResidentIdOrderByCreatedAtDesc(resident.getResidentId()));
    }

    @PutMapping({"/read/{id}", "/{id}/read"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER', 'RESIDENT')")
    public ResponseEntity<MessageResponse> markNotificationRead(@PathVariable("id") Long id) {
        Notification notification = notificationRepository.findById(id).orElse(null);
        if (notification != null) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
        return ResponseEntity.ok(new MessageResponse("Notification marked as read"));
    }
}
