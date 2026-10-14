package com.smartwater.billing.owner.controller;

import com.smartwater.billing.owner.dto.BuildingDTOs.*;
import com.smartwater.billing.common.dto.DashboardDTOs.OwnerDashboardStats;
import com.smartwater.billing.common.dto.DashboardDTOs.OwnerUsageComparisonResponse;
import com.smartwater.billing.resident.dto.ResidentDTOs.*;
import com.smartwater.billing.owner.service.BuildingOwnerService;
import com.smartwater.billing.resident.service.ResidentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            @RequestParam(value = "buildingId", required = false) Long buildingId
    ) {
        return ResponseEntity.ok(residentService.getOwnerUsageComparison(authentication.getName(), buildingId));
    }

    @PutMapping({"/owners/residents/{id}", "/api/owner/residents/{id}"})
    public ResponseEntity<ResidentResponse> updateResident(
            Authentication authentication,
            @PathVariable("id") Long residentId,
            @Valid @RequestBody UpdateResidentRequest request
    ) {
        return ResponseEntity.ok(residentService.updateResident(authentication.getName(), residentId, request));
    }

    @DeleteMapping({"/owners/residents/{id}", "/api/owner/residents/{id}"})
    public ResponseEntity<Void> deleteResident(
            Authentication authentication,
            @PathVariable("id") Long residentId
    ) {
        residentService.deleteResident(authentication.getName(), residentId);
        return ResponseEntity.noContent().build();
    }
}
