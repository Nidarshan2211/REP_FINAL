package com.smartwater.billing.owner.controller;

import com.smartwater.billing.common.entity.BillingCycle;
import com.smartwater.billing.owner.entity.Building;
import com.smartwater.billing.owner.entity.BuildingOwner;
import com.smartwater.billing.common.entity.Invoice;
import com.smartwater.billing.common.exception.BadRequestException;
import com.smartwater.billing.common.exception.ResourceNotFoundException;
import com.smartwater.billing.owner.repository.BuildingRepository;
import com.smartwater.billing.common.service.BillingCycleService;
import com.smartwater.billing.owner.service.BuildingOwnerService;
import com.smartwater.billing.common.service.InvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class BillingCycleController {

    private final BillingCycleService billingCycleService;
    private final InvoiceService invoiceService;
    private final BuildingOwnerService buildingOwnerService;
    private final BuildingRepository buildingRepository;

    public BillingCycleController(BillingCycleService billingCycleService, InvoiceService invoiceService, BuildingOwnerService buildingOwnerService, BuildingRepository buildingRepository) {
        this.billingCycleService = billingCycleService;
        this.invoiceService = invoiceService;
        this.buildingOwnerService = buildingOwnerService;
        this.buildingRepository = buildingRepository;
    }


    @PostMapping({"/billing-cycles", "/owner/billing-cycles", "/admin/billing/open", "/api/admin/billing/open", "/api/billing-cycles", "/api/owner/billing-cycles"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<BillingCycle> openCycle(Authentication authentication, @RequestBody Map<String, Object> body) {
        if (body == null || !body.containsKey("buildingId") || body.get("buildingId") == null || body.get("buildingId").toString().trim().isEmpty()) {
            throw new BadRequestException("buildingId is required. Silent building selection fallback has been disabled.");
        }

        Long buildingId;
        try {
            buildingId = Long.valueOf(body.get("buildingId").toString().trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid numeric buildingId: '" + body.get("buildingId") + "'.");
        }

        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + buildingId));

        boolean isSuperAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (!isSuperAdmin && authentication != null) {
            BuildingOwner owner = buildingOwnerService.getOwnerByUserEmail(authentication.getName());
            if (building.getOwner() == null || !building.getOwner().getOwnerId().equals(owner.getOwnerId())) {
                throw new org.springframework.security.access.AccessDeniedException("Access Denied: You do not own building '" + building.getBuildingName() + "'.");
            }
        }

        LocalDate startDate = body.get("startDate") != null ? LocalDate.parse(body.get("startDate").toString()) : LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = body.get("endDate") != null ? LocalDate.parse(body.get("endDate").toString()) : startDate.plusMonths(1).withDayOfMonth(1).minusDays(1);

        return ResponseEntity.ok(billingCycleService.openCycle(buildingId, startDate, endDate));
    }

    @PostMapping({"/billing-cycles/{cycleId}/finalize", "/owner/billing-cycles/{cycleId}/finalize", "/admin/billing/finalize/{cycleId}", "/api/admin/billing/finalize/{cycleId}", "/api/billing-cycles/{cycleId}/finalize", "/api/owner/billing-cycles/{cycleId}/finalize"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<BillingCycle> finalizeCycle(@PathVariable("cycleId") Long cycleId) {
        return ResponseEntity.ok(billingCycleService.finalizeCycle(cycleId));
    }

    @PostMapping({"/billing-cycles/{cycleId}/archive", "/owner/billing-cycles/{cycleId}/archive", "/admin/billing/archive/{cycleId}", "/api/admin/billing/archive/{cycleId}", "/api/billing-cycles/{cycleId}/archive", "/api/owner/billing-cycles/{cycleId}/archive"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<BillingCycle> archiveCycle(@PathVariable("cycleId") Long cycleId) {
        return ResponseEntity.ok(billingCycleService.archiveCycle(cycleId));
    }

    @GetMapping({"/billing-cycles", "/owner/billing-cycles", "/billing-cycles/building/{buildingId}", "/admin/billing/cycles", "/api/admin/billing/cycles", "/api/billing-cycles", "/api/owner/billing-cycles", "/api/billing-cycles/building/{buildingId}"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<List<BillingCycle>> getCycles(
            Authentication authentication,
            @PathVariable(value = "buildingId", required = false) Long pathBuildingId,
            @RequestParam(value = "buildingId", required = false) Long paramBuildingId
    ) {
        Long rawId = pathBuildingId != null ? pathBuildingId : paramBuildingId;
        Long resolvedId = resolveBuildingId(authentication, rawId);
        return ResponseEntity.ok(billingCycleService.getBuildingCycles(resolvedId));
    }

    @GetMapping({"/billing-cycles/{cycleId}/invoices", "/billing-cycles/{cycleId}/bills", "/owner/billing-cycles/{cycleId}/bills", "/api/admin/billing/invoices/{cycleId}", "/api/billing-cycles/{cycleId}/invoices", "/api/owner/billing-cycles/{cycleId}/bills"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<List<Invoice>> getCycleInvoices(@PathVariable("cycleId") Long cycleId) {
        return ResponseEntity.ok(invoiceService.getCycleInvoices(cycleId));
    }

    private Long resolveBuildingId(Authentication authentication, Object obj) {
        if (obj != null && !obj.toString().isEmpty()) {
            try { return Long.valueOf(obj.toString()); } catch (Exception ignored) {}
        }
        if (authentication != null) {
            try {
                var buildings = buildingOwnerService.getOwnerBuildings(authentication.getName());
                if (!buildings.isEmpty()) {
                    return buildings.get(0).getBuildingId();
                }
            } catch (Exception ignored) {}
        }
        List<Building> all = buildingRepository.findAll();
        if (!all.isEmpty()) return all.get(0).getBuildingId();
        return 1L;
    }
}
