package com.smartwater.billing.resident.controller;

import com.smartwater.billing.common.dto.AuthDTOs.MessageResponse;
import com.smartwater.billing.common.dto.BillingDTOs.*;
import com.smartwater.billing.common.dto.DashboardDTOs.ResidentDashboardStats;
import com.smartwater.billing.common.dto.DashboardDTOs.ResidentComparisonResponse;
import com.smartwater.billing.resident.dto.ResidentDTOs.*;
import com.smartwater.billing.common.dto.UsageDTOs.UsageResponse;
import com.smartwater.billing.resident.entity.Notification;
import com.smartwater.billing.resident.entity.Resident;
import com.smartwater.billing.resident.repository.NotificationRepository;
import com.smartwater.billing.resident.repository.ResidentRepository;
import com.smartwater.billing.common.service.BillingService;
import com.smartwater.billing.resident.service.ResidentService;
import com.smartwater.billing.common.service.WaterUsageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class ResidentController {

    private final ResidentService residentService;
    private final BillingService billingService;
    private final WaterUsageService usageService;
    private final NotificationRepository notificationRepository;
    private final ResidentRepository residentRepository;

    public ResidentController(ResidentService residentService, BillingService billingService, WaterUsageService usageService, NotificationRepository notificationRepository, ResidentRepository residentRepository) {
        this.residentService = residentService;
        this.billingService = billingService;
        this.usageService = usageService;
        this.notificationRepository = notificationRepository;
        this.residentRepository = residentRepository;
    }


    @PostMapping({"/residents/invite", "/api/residents/invite"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<ResidentResponse> inviteResident(
            Authentication authentication,
            @Valid @RequestBody InviteResidentRequest request
    ) {
        return ResponseEntity.ok(residentService.inviteResident(authentication.getName(), request));
    }

    @PostMapping({"/residents/invitations/{id}/resend", "/api/residents/invitations/{id}/resend"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<MessageResponse> resendInvitation(
            Authentication authentication,
            @PathVariable("id") Long residentId
    ) {
        return ResponseEntity.ok(residentService.resendInvitation(authentication.getName(), residentId));
    }

    @GetMapping({"/resident/dashboard", "/residents/dashboard", "/api/resident/dashboard", "/api/residents/dashboard"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER', 'RESIDENT')")
    public ResponseEntity<ResidentDashboardStats> getDashboardStats(Authentication authentication) {
        return ResponseEntity.ok(residentService.getResidentDashboardStats(authentication.getName()));
    }

    @GetMapping({"/resident/consumption-comparison", "/consumption-comparison", "/api/resident/consumption-comparison"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER', 'RESIDENT')")
    public ResponseEntity<ResidentComparisonResponse> getConsumptionComparison(Authentication authentication) {
        return ResponseEntity.ok(residentService.getResidentConsumptionComparison(authentication.getName()));
    }

    @GetMapping({"/resident/bills", "/bills/resident", "/api/resident/bills", "/api/bills/resident"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER', 'RESIDENT')")
    public ResponseEntity<List<BillResponse>> getBills(Authentication authentication) {
        return ResponseEntity.ok(billingService.getResidentBills(authentication.getName()));
    }

    @GetMapping({"/resident/payments", "/payments/resident", "/api/resident/payments", "/api/payments/resident"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER', 'RESIDENT')")
    public ResponseEntity<List<PaymentResponse>> getPayments(Authentication authentication) {
        return ResponseEntity.ok(billingService.getResidentPayments(authentication.getName()));
    }

    @PostMapping({"/resident/pay", "/bills/pay", "/api/resident/pay"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER', 'RESIDENT')")
    public ResponseEntity<PaymentResponse> payBill(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(billingService.processPayment(request));
    }

    @GetMapping({"/resident/usage", "/usage/resident", "/api/resident/usage"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER', 'RESIDENT')")
    public ResponseEntity<List<UsageResponse>> getUsage(Authentication authentication) {
        return ResponseEntity.ok(usageService.getResidentUsageRecords(authentication.getName()));
    }

    @GetMapping({"/resident/notifications", "/api/resident/notifications"})
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

    @PutMapping({"/resident/notifications/{id}/read", "/api/resident/notifications/{id}/read"})
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
