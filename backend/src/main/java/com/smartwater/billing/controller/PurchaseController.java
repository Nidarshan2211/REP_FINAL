package com.smartwater.billing.controller;

import com.smartwater.billing.dto.AuthDTOs.MessageResponse;
import com.smartwater.billing.dto.BillingEngineDTOs.PurchaseSummaryResponse;
import com.smartwater.billing.entity.Building;
import com.smartwater.billing.entity.WaterPurchase;
import com.smartwater.billing.repository.BuildingRepository;
import com.smartwater.billing.service.BuildingOwnerService;
import com.smartwater.billing.service.WaterPurchaseCostService;
import com.smartwater.billing.service.WaterPurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class PurchaseController {

    private final WaterPurchaseService waterPurchaseService;
    private final WaterPurchaseCostService waterPurchaseCostService;
    private final BuildingRepository buildingRepository;
    private final BuildingOwnerService buildingOwnerService;

    @PostMapping({"/water-purchases", "/purchases", "/admin/purchase", "/api/admin/purchase", "/api/water-purchases"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<WaterPurchase> recordPurchase(Authentication authentication, @RequestBody Map<String, Object> body) {
        Long buildingId = resolveBuildingId(authentication, body.get("buildingId"));

        Long cycleId = body.get("cycleId") != null && !body.get("cycleId").toString().isEmpty() 
                ? Long.valueOf(body.get("cycleId").toString()) 
                : body.get("billingCycleId") != null && !body.get("billingCycleId").toString().isEmpty()
                ? Long.valueOf(body.get("billingCycleId").toString()) : null;

        String supplier = body.get("supplier") != null ? body.get("supplier").toString() : "Municipal Water Supply";

        BigDecimal volume = new BigDecimal("1000.00");
        if (body.get("volume") != null && !body.get("volume").toString().isEmpty()) {
            volume = new BigDecimal(body.get("volume").toString());
        } else if (body.get("quantity") != null && !body.get("quantity").toString().isEmpty()) {
            volume = new BigDecimal(body.get("quantity").toString());
        }

        BigDecimal unitCost = new BigDecimal("50.00");
        if (body.get("unitCost") != null && !body.get("unitCost").toString().isEmpty()) {
            unitCost = new BigDecimal(body.get("unitCost").toString());
        }

        LocalDate purchaseDate = LocalDate.now();
        if (body.get("purchaseDate") != null && !body.get("purchaseDate").toString().isEmpty()) {
            purchaseDate = LocalDate.parse(body.get("purchaseDate").toString());
        }

        return ResponseEntity.ok(waterPurchaseService.recordPurchase(buildingId, cycleId, supplier, volume, unitCost, purchaseDate));
    }

    @GetMapping({"/water-purchases", "/purchases", "/water-purchases/building/{buildingId}", "/purchases/building/{buildingId}", "/admin/purchase", "/api/admin/purchase", "/api/water-purchases", "/api/water-purchases/building/{buildingId}"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<List<WaterPurchase>> getPurchases(
            Authentication authentication,
            @PathVariable(value = "buildingId", required = false) Long pathBuildingId,
            @RequestParam(value = "buildingId", required = false) Long paramBuildingId
    ) {
        Long rawId = pathBuildingId != null ? pathBuildingId : paramBuildingId;
        Long resolvedId = resolveBuildingId(authentication, rawId);

        return ResponseEntity.ok(waterPurchaseService.getPurchases(resolvedId));
    }

    @GetMapping({"/water-purchases/cycle/{billingCycleId}", "/purchases/cycle/{billingCycleId}", "/api/water-purchases/cycle/{billingCycleId}"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<List<WaterPurchase>> getPurchasesByCycle(@PathVariable("billingCycleId") Long billingCycleId) {
        return ResponseEntity.ok(waterPurchaseService.getPurchasesByCycle(billingCycleId));
    }

    @GetMapping({
            "/water-purchases/summary/{buildingId}/{billingCycleId}",
            "/water-purchases/summary/{buildingId}",
            "/purchases/building/{buildingId}/summary",
            "/purchases/summary/{buildingId}",
            "/purchases/summary/{buildingId}/{billingCycleId}",
            "/api/water-purchases/summary/{buildingId}/{billingCycleId}",
            "/api/water-purchases/summary/{buildingId}"
    })
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<PurchaseSummaryResponse> getPurchaseSummary(
            @PathVariable("buildingId") Long buildingId,
            @PathVariable(value = "billingCycleId", required = false) Long billingCycleId
    ) {
        return ResponseEntity.ok(waterPurchaseCostService.calculatePurchaseSummary(buildingId, billingCycleId));
    }

    @DeleteMapping({"/water-purchases/{id}", "/purchases/{id}", "/admin/purchase/{id}", "/api/admin/purchase/{id}", "/api/water-purchases/{id}"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<MessageResponse> deletePurchase(@PathVariable("id") Long id) {
        waterPurchaseService.deletePurchase(id);
        return ResponseEntity.ok(new MessageResponse("Water purchase record deleted successfully."));
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
