package com.smartwater.billing.controller;

import com.smartwater.billing.dto.AuthDTOs.MessageResponse;
import com.smartwater.billing.entity.Notification;
import com.smartwater.billing.entity.Resident;
import com.smartwater.billing.repository.NotificationRepository;
import com.smartwater.billing.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/notifications", "/api/notifications"})
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final ResidentRepository residentRepository;

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
