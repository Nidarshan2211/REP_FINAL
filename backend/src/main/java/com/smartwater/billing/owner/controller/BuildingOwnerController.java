package com.smartwater.billing.owner.controller;

import com.smartwater.billing.owner.dto.BuildingDTOs.*;
import com.smartwater.billing.common.dto.DashboardDTOs.OwnerDashboardStats;
import com.smartwater.billing.common.dto.DashboardDTOs.OwnerUsageComparisonResponse;
import com.smartwater.billing.common.dto.DashboardDTOs.ResidentHistoryItemDTO;
import com.smartwater.billing.resident.dto.ResidentDTOs.*;
import com.smartwater.billing.owner.service.BuildingOwnerService;
import com.smartwater.billing.resident.service.ResidentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
public class BuildingOwnerController {

    private final BuildingOwnerService ownerService;
    private final ResidentService residentService;

    public BuildingOwnerController(BuildingOwnerService ownerService, ResidentService residentService) {
        this.ownerService = ownerService;
        this.residentService = residentService;
    }

    @GetMapping({"/owners/dashboard", "/api/owner/dashboard"})
    public ResponseEntity<OwnerDashboardStats> getDashboardStats(Authentication authentication) {
        return ResponseEntity.ok(ownerService.getOwnerDashboardStats(authentication.getName()));
    }

    @GetMapping({"/owners/buildings", "/api/owner/buildings"})
    public ResponseEntity<List<BuildingResponse>> getBuildings(Authentication authentication) {
        return ResponseEntity.ok(ownerService.getOwnerBuildings(authentication.getName()));
    }

    @PostMapping({"/owners/buildings", "/api/owner/buildings"})
    public ResponseEntity<BuildingResponse> createBuilding(
            Authentication authentication,
            @Valid @RequestBody CreateBuildingRequest request
    ) {
        return ResponseEntity.ok(ownerService.createBuilding(authentication.getName(), request));
    }

    @DeleteMapping({"/owners/buildings/{id}", "/api/owner/buildings/{id}"})
    public ResponseEntity<Void> deleteBuilding(
            Authentication authentication,
            @PathVariable("id") Long buildingId
    ) {
        ownerService.deleteBuilding(authentication.getName(), buildingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping({"/owners/residents", "/residents/owner", "/api/owner/residents", "/api/residents/owner"})
    public ResponseEntity<List<ResidentResponse>> getResidents(
            Authentication authentication,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "status", required = false) String status
    ) {
        return ResponseEntity.ok(residentService.getOwnerResidents(authentication.getName(), query, status));
    }

    @GetMapping({"/owners/invitations", "/residents/invitations", "/api/residents/invitations", "/api/owner/invitations"})
    public ResponseEntity<List<ResidentResponse>> getInvitations(
            Authentication authentication,
            @RequestParam(value = "query", required = false) String query
    ) {
        return ResponseEntity.ok(residentService.getOwnerResidents(authentication.getName(), query, null));
    }

    @GetMapping({"/owners/usage-comparison", "/api/owner/usage-comparison"})
    public ResponseEntity<OwnerUsageComparisonResponse> getUsageComparison(
            Authentication authentication,
            @RequestParam(value = "buildingId", required = false) Long buildingId,
            @RequestParam(value = "billingCycleId", required = false) Long billingCycleId
    ) {
        return ResponseEntity.ok(residentService.getOwnerUsageComparison(authentication.getName(), buildingId, billingCycleId));
    }

    @PutMapping({"/owners/residents/{id}", "/api/owner/residents/{id}"})
    public ResponseEntity<ResidentResponse> updateResident(
            Authentication authentication,
            @PathVariable("id") Long residentId,
            @Valid @RequestBody UpdateResidentRequest request
    ) {
        return ResponseEntity.ok(residentService.updateResident(authentication.getName(), residentId, request));
    }

    @PostMapping({"/owners/residents/{id}/vacate", "/api/owner/residents/{id}/vacate"})
    public ResponseEntity<Void> vacateResident(
            Authentication authentication,
            @PathVariable("id") Long residentId
    ) {
        residentService.vacateResident(authentication.getName(), residentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping({"/owners/residents/{id}", "/api/owner/residents/{id}"})
    public ResponseEntity<Void> deleteResident(
            Authentication authentication,
            @PathVariable("id") Long residentId
    ) {
        residentService.deleteResident(authentication.getName(), residentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping({"/owners/history", "/owner/history", "/api/owner/history", "/api/owners/history", "/api/owner/resident-history"})
    public ResponseEntity<List<ResidentHistoryItemDTO>> getResidentHistory(Authentication authentication) {
        return ResponseEntity.ok(ownerService.getResidentHistory(authentication.getName()));
    }

    @DeleteMapping({"/owners/leave", "/owner/leave", "/api/owner/leave", "/api/owners/leave"})
    public ResponseEntity<?> leaveApplication(Authentication authentication) {
        ownerService.leaveApplication(authentication.getName());
        return ResponseEntity.ok(Map.of("message", "You have successfully left the platform. Your account and properties have been removed."));
    }
}

