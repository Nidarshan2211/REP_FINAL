package com.smartwater.billing.resident.controller;

import com.smartwater.billing.common.dto.AuthDTOs.MessageResponse;
import com.smartwater.billing.common.dto.LeakDTOs.LeakFeedbackRequest;
import com.smartwater.billing.common.dto.LeakDTOs.LeakFeedbackResponse;
import com.smartwater.billing.common.dto.LeakDTOs.ResidentLeakStatusResponse;
import com.smartwater.billing.common.ml.LeakFeedbackService;
import com.smartwater.billing.common.ml.LeakReportService;
import com.smartwater.billing.resident.dto.HouseholdDTOs.*;
import com.smartwater.billing.resident.service.AwayPeriodService;
import com.smartwater.billing.resident.service.HouseholdService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class ResidentLeakAndHouseholdController {

    private final HouseholdService householdService;
    private final AwayPeriodService awayPeriodService;
    private final LeakReportService leakReportService;
    private final LeakFeedbackService leakFeedbackService;

    public ResidentLeakAndHouseholdController(HouseholdService householdService,
                                              AwayPeriodService awayPeriodService,
                                              LeakReportService leakReportService,
                                              LeakFeedbackService leakFeedbackService) {
        this.householdService = householdService;
        this.awayPeriodService = awayPeriodService;
        this.leakReportService = leakReportService;
        this.leakFeedbackService = leakFeedbackService;
    }

    // -------------------------------------------------------------------------
    // Household Size Management
    // -------------------------------------------------------------------------

    @GetMapping({"/resident/household", "/api/resident/household"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER', 'RESIDENT')")
    public ResponseEntity<HouseholdDetailResponse> getHouseholdDetails(Authentication authentication) {
        return ResponseEntity.ok(householdService.getResidentHouseholdDetails(authentication.getName()));
    }

    @RequestMapping(value = {"/resident/household", "/api/resident/household"}, method = {RequestMethod.POST, RequestMethod.PUT})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER', 'RESIDENT')")
    public ResponseEntity<HouseholdDetailResponse> updateHousehold(
            Authentication authentication,
            @Valid @RequestBody HouseholdUpdateRequest request
    ) {
        return ResponseEntity.ok(householdService.updateResidentHousehold(authentication.getName(), request));
    }

    @RequestMapping(value = {"/resident/household/confirm", "/api/resident/household/confirm"}, method = {RequestMethod.POST, RequestMethod.PUT})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER', 'RESIDENT')")
    public ResponseEntity<HouseholdDetailResponse> confirmHousehold(Authentication authentication) {
        return ResponseEntity.ok(householdService.confirmResidentHousehold(authentication.getName()));
    }

    // -------------------------------------------------------------------------
    // Away Periods Management
    // -------------------------------------------------------------------------

    @GetMapping({"/resident/away-periods", "/api/resident/away-periods"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER', 'RESIDENT')")
    public ResponseEntity<List<AwayPeriodResponse>> getAwayPeriods(Authentication authentication) {
        return ResponseEntity.ok(awayPeriodService.getResidentAwayPeriods(authentication.getName()));
    }

    @PostMapping({"/resident/away-periods", "/api/resident/away-periods"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER', 'RESIDENT')")
    public ResponseEntity<AwayPeriodResponse> createAwayPeriod(
            Authentication authentication,
            @Valid @RequestBody AwayPeriodRequest request
    ) {
        return ResponseEntity.ok(awayPeriodService.createAwayPeriod(authentication.getName(), request));
    }

    @DeleteMapping({"/resident/away-periods/{id}", "/api/resident/away-periods/{id}"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER', 'RESIDENT')")
    public ResponseEntity<MessageResponse> deleteAwayPeriod(
            Authentication authentication,
            @PathVariable("id") Long id
    ) {
        awayPeriodService.deleteAwayPeriod(authentication.getName(), id);
        return ResponseEntity.ok(new MessageResponse("Away period removed successfully."));
    }

    // -------------------------------------------------------------------------
    // ML Leak Detection & Feedback
    // -------------------------------------------------------------------------

    @GetMapping({"/resident/leak-status", "/api/resident/leak-status"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER', 'RESIDENT')")
    public ResponseEntity<ResidentLeakStatusResponse> getLeakStatus(Authentication authentication) {
        return ResponseEntity.ok(leakReportService.getResidentLeakStatus(authentication.getName()));
    }

    @PostMapping({"/resident/leak-feedback", "/api/resident/leak-feedback"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER', 'RESIDENT')")
    public ResponseEntity<LeakFeedbackResponse> submitLeakFeedback(
            Authentication authentication,
            @RequestParam("usageId") Long usageId,
            @Valid @RequestBody LeakFeedbackRequest request
    ) {
        return ResponseEntity.ok(leakFeedbackService.submitFeedback(authentication.getName(), usageId, request));
    }
}
