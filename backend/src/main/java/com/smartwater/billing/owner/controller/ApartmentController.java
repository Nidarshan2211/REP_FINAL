package com.smartwater.billing.owner.controller;

import com.smartwater.billing.owner.dto.ApartmentDTOs.*;
import com.smartwater.billing.resident.dto.OccupancyDTOs.*;
import com.smartwater.billing.resident.dto.OccupancyDTOs.AvailableApartmentResponse;
import com.smartwater.billing.owner.service.ApartmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class ApartmentController {

    private final ApartmentService apartmentService;

    public ApartmentController(ApartmentService apartmentService) {
        this.apartmentService = apartmentService;
    }

    @GetMapping({"/apartments/owners", "/api/apartments/owners"})
    public ResponseEntity<List<PublicOwnerResponse>> getPublicOwners() {
        return ResponseEntity.ok(apartmentService.getPublicOwners());
    }

    @GetMapping({"/apartments/owners/{ownerId}/buildings", "/api/apartments/owners/{ownerId}/buildings"})
    public ResponseEntity<List<PublicBuildingResponse>> getOwnerBuildings(@PathVariable("ownerId") Long ownerId) {
        return ResponseEntity.ok(apartmentService.getOwnerBuildings(ownerId));
    }

    @GetMapping({"/apartments/buildings", "/api/apartments/buildings"})
    public ResponseEntity<List<PublicBuildingResponse>> getAllPublicBuildings() {
        return ResponseEntity.ok(apartmentService.getAllPublicBuildings());
    }

    @GetMapping({"/apartments/available", "/api/apartments/available"})
    public ResponseEntity<List<AvailableApartmentResponse>> getAvailableApartments(
            @RequestParam(value = "buildingId", required = false) Long buildingId,
            @RequestParam(value = "floorNumber", required = false) Integer floorNumber
    ) {
        return ResponseEntity.ok(apartmentService.getAvailableApartments(buildingId, floorNumber));
    }

    @GetMapping({"/owners/buildings/{buildingId}/apartments", "/api/owner/buildings/{buildingId}/apartments"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<BuildingInventoryResponse> getBuildingInventory(
            Authentication authentication,
            @PathVariable("buildingId") Long buildingId
    ) {
        return ResponseEntity.ok(apartmentService.getBuildingInventory(authentication.getName(), buildingId));
    }

    @PostMapping({"/owners/buildings/{buildingId}/apartments", "/api/owner/buildings/{buildingId}/apartments"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<ApartmentResponse> createApartment(
            Authentication authentication,
            @PathVariable("buildingId") Long buildingId,
            @Valid @RequestBody CreateApartmentRequest request
    ) {
        return ResponseEntity.ok(apartmentService.createApartment(authentication.getName(), buildingId, request));
    }

    @PostMapping({"/owners/buildings/{buildingId}/floors/batch-apartments", "/api/owner/buildings/{buildingId}/floors/batch-apartments"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<List<ApartmentResponse>> batchCreateApartments(
            Authentication authentication,
            @PathVariable("buildingId") Long buildingId,
            @Valid @RequestBody BatchCreateApartmentsRequest request
    ) {
        request.setBuildingId(buildingId);
        return ResponseEntity.ok(apartmentService.batchCreateApartments(authentication.getName(), request));
    }

    @GetMapping({"/owners/apartments", "/api/owner/apartments"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<List<ApartmentResponse>> getAllOwnerApartments(Authentication authentication) {
        return ResponseEntity.ok(apartmentService.getAllOwnerApartments(authentication.getName()));
    }

    @DeleteMapping({"/owners/apartments/{apartmentId}", "/api/owner/apartments/{apartmentId}"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<Void> deleteApartment(
            Authentication authentication,
            @PathVariable("apartmentId") Long apartmentId
    ) {
        apartmentService.deleteApartment(authentication.getName(), apartmentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping({"/owners/apartments/{apartmentId}/vacate", "/api/owner/apartments/{apartmentId}/vacate"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<Void> vacateApartment(
            Authentication authentication,
            @PathVariable("apartmentId") Long apartmentId
    ) {
        apartmentService.vacateApartment(authentication.getName(), apartmentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping({"/owners/invitations/{invitationId}", "/api/owner/invitations/{invitationId}"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<Void> cancelInvitation(
            Authentication authentication,
            @PathVariable("invitationId") Long invitationId
    ) {
        apartmentService.cancelInvitation(authentication.getName(), invitationId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping({"/owners/apartments/{apartmentId}/meter", "/api/owner/apartments/{apartmentId}/meter"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<ApartmentResponse> updateApartmentMeter(
            Authentication authentication,
            @PathVariable("apartmentId") Long apartmentId,
            @RequestBody UpdateApartmentMeterRequest request
    ) {
        return ResponseEntity.ok(apartmentService.updateApartmentMeter(authentication.getName(), apartmentId, request.getMeterNumber()));
    }

    @PostMapping({"/owners/buildings/{buildingId}/generate-default-apartments", "/api/owner/buildings/{buildingId}/generate-default-apartments"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<BuildingInventoryResponse> generateDefaultApartments(
            Authentication authentication,
            @PathVariable("buildingId") Long buildingId
    ) {
        return ResponseEntity.ok(apartmentService.generateMissingUnitsForBuilding(authentication.getName(), buildingId));
    }
}
