package com.smartwater.billing.superadmin.controller;

import com.smartwater.billing.common.dto.DashboardDTOs.AdminDashboardStats;
import com.smartwater.billing.common.dto.DashboardDTOs.BuildingOwnerDTO;
import com.smartwater.billing.common.dto.DashboardDTOs.SystemHistoryItemDTO;
import com.smartwater.billing.superadmin.service.SuperAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/admin", "/api/admin"})
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminController {

    private final SuperAdminService adminService;

    public SuperAdminController(SuperAdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardStats> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/owners")
    public ResponseEntity<List<BuildingOwnerDTO>> getAllOwners() {
        return ResponseEntity.ok(adminService.getAllOwners());
    }

    @GetMapping({"/owners/pending", "/pending-owners"})
    public ResponseEntity<List<BuildingOwnerDTO>> getPendingOwners() {
        return ResponseEntity.ok(adminService.getPendingOwners());
    }

    @PutMapping("/owners/{id}/approve")
    public ResponseEntity<BuildingOwnerDTO> approveOwner(@PathVariable("id") Long ownerId) {
        return ResponseEntity.ok(adminService.approveOwner(ownerId));
    }

    @PutMapping("/owners/{id}/reject")
    public ResponseEntity<BuildingOwnerDTO> rejectOwner(@PathVariable("id") Long ownerId) {
        return ResponseEntity.ok(adminService.rejectOwner(ownerId));
    }

    @DeleteMapping("/owners/{id}")
    public ResponseEntity<Void> deleteOwner(@PathVariable("id") Long ownerId) {
        adminService.deleteOwner(ownerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history")
    public ResponseEntity<List<SystemHistoryItemDTO>> getSystemHistory() {
        return ResponseEntity.ok(adminService.getSystemHistory());
    }
}
