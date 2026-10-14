package com.smartwater.billing.resident.controller;

import com.smartwater.billing.resident.dto.OccupancyDTOs.*;
import com.smartwater.billing.resident.entity.OccupancyRequest;
import com.smartwater.billing.resident.service.OccupancyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER', 'RESIDENT')")
public class OccupancyController {

    private final OccupancyService occupancyService;

    public OccupancyController(OccupancyService occupancyService) {
        this.occupancyService = occupancyService;
    }

    @PostMapping({"/resident/claim-apartment", "/api/resident/claim-apartment", "/resident/occupancy/select", "/api/resident/occupancy/select"})
    public ResponseEntity<OccupancyResponse> claimApartment(
            Authentication authentication,
            @Valid @RequestBody SelectApartmentRequest request
    ) {
        return ResponseEntity.ok(occupancyService.claimApartment(authentication.getName(), request.getApartmentId()));
    }

    @GetMapping({"/resident/occupancy/current", "/api/resident/occupancy/current"})
    public ResponseEntity<OccupancyResponse> getCurrentOccupancy(Authentication authentication) {
        OccupancyResponse resp = occupancyService.getCurrentOccupancy(authentication.getName());
        return ResponseEntity.ok(resp);
    }

    @GetMapping({"/resident/occupancy/history", "/api/resident/occupancy/history"})
    public ResponseEntity<List<OccupancyResponse>> getOccupancyHistory(Authentication authentication) {
        return ResponseEntity.ok(occupancyService.getOccupancyHistory(authentication.getName()));
    }

    @PostMapping({"/resident/move-out", "/api/resident/move-out", "/resident/occupancy/move-out", "/api/resident/occupancy/move-out"})
    public ResponseEntity<MoveOutResponse> moveOut(
            Authentication authentication,
            @Valid @RequestBody MoveOutRequest request
    ) {
        return ResponseEntity.ok(occupancyService.moveOut(authentication.getName(), request));
    }

    // Resident Occupancy Request Flow
    @PostMapping({"/resident/occupancy-request", "/api/resident/occupancy-request"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'RESIDENT')")
    public ResponseEntity<OccupancyRequestResponse> submitOccupancyRequest(
            Authentication authentication,
            @Valid @RequestBody SubmitOccupancyRequest request
    ) {
        return ResponseEntity.ok(occupancyService.submitOccupancyRequest(authentication.getName(), request));
    }

    @PostMapping({"/resident/occupancy-request/{requestId}/cancel", "/api/resident/occupancy-request/{requestId}/cancel"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'RESIDENT')")
    public ResponseEntity<OccupancyRequestResponse> cancelOccupancyRequest(
            Authentication authentication,
            @PathVariable("requestId") Long requestId
    ) {
        return ResponseEntity.ok(occupancyService.cancelOccupancyRequest(authentication.getName(), requestId));
    }

    @GetMapping({"/resident/occupancy-request/pending", "/api/resident/occupancy-request/pending"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'RESIDENT')")
    public ResponseEntity<OccupancyRequestResponse> getResidentPendingRequest(Authentication authentication) {
        return ResponseEntity.ok(occupancyService.getResidentPendingRequest(authentication.getName()));
    }

    @GetMapping({"/resident/occupancy-requests", "/api/resident/occupancy-requests"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'RESIDENT')")
    public ResponseEntity<List<OccupancyRequestResponse>> getAllResidentRequests(Authentication authentication) {
        return ResponseEntity.ok(occupancyService.getAllResidentRequests(authentication.getName()));
    }

    // Owner Review Workflow
    @GetMapping({"/owner/occupancy-requests", "/api/owner/occupancy-requests"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<List<OccupancyRequestResponse>> getOwnerOccupancyRequests(
            Authentication authentication,
            @RequestParam(value = "status", required = false) OccupancyRequest.RequestStatus status
    ) {
        return ResponseEntity.ok(occupancyService.getOwnerOccupancyRequests(authentication.getName(), status));
    }

    @PostMapping({"/owner/occupancy-requests/{requestId}/review", "/api/owner/occupancy-requests/{requestId}/review"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<OccupancyRequestResponse> reviewOccupancyRequest(
            Authentication authentication,
            @PathVariable("requestId") Long requestId,
            @Valid @RequestBody ReviewOccupancyRequest request
    ) {
        return ResponseEntity.ok(occupancyService.reviewOccupancyRequest(authentication.getName(), requestId, request));
    }
}
