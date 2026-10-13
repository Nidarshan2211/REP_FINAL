package com.smartwater.billing.controller;

import com.smartwater.billing.entity.BillingCycle;
import com.smartwater.billing.entity.Building;
import com.smartwater.billing.entity.Invoice;
import com.smartwater.billing.repository.BuildingRepository;
import com.smartwater.billing.service.BillingCycleService;
import com.smartwater.billing.service.BuildingOwnerService;
import com.smartwater.billing.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class BillingCycleController {

    private final BillingCycleService billingCycleService;
    private final InvoiceService invoiceService;
    private final BuildingOwnerService buildingOwnerService;
    private final BuildingRepository buildingRepository;

    @PostMapping({"/billing-cycles", "/owner/billing-cycles", "/admin/billing/open", "/api/admin/billing/open", "/api/billing-cycles", "/api/owner/billing-cycles"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<BillingCycle> openCycle(Authentication authentication, @RequestBody Map<String, Object> body) {
        Long buildingId = resolveBuildingId(authentication, body.get("buildingId"));
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
