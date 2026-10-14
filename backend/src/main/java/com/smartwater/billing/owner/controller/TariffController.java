package com.smartwater.billing.owner.controller;

import com.smartwater.billing.owner.dto.BillingEngineDTOs.TariffRecommendationResponse;
import com.smartwater.billing.owner.entity.Building;
import com.smartwater.billing.owner.entity.TariffPlan;
import com.smartwater.billing.owner.repository.BuildingRepository;
import com.smartwater.billing.owner.service.BuildingOwnerService;
import com.smartwater.billing.owner.service.TariffCalculationService;
import com.smartwater.billing.owner.service.TariffPlanService;
import com.smartwater.billing.owner.service.TariffRecommendationService;
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
public class TariffController {

    private final TariffPlanService tariffPlanService;
    private final TariffCalculationService tariffCalculationService;
    private final TariffRecommendationService tariffRecommendationService;
    private final BuildingOwnerService buildingOwnerService;
    private final BuildingRepository buildingRepository;

    public TariffController(TariffPlanService tariffPlanService, TariffCalculationService tariffCalculationService, TariffRecommendationService tariffRecommendationService, BuildingOwnerService buildingOwnerService, BuildingRepository buildingRepository) {
        this.tariffPlanService = tariffPlanService;
        this.tariffCalculationService = tariffCalculationService;
        this.tariffRecommendationService = tariffRecommendationService;
        this.buildingOwnerService = buildingOwnerService;
        this.buildingRepository = buildingRepository;
    }


    @PostMapping({"/tariff-plans", "/tariffs", "/admin/tariff", "/api/admin/tariff", "/api/tariff-plans"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<TariffPlan> createTariff(Authentication authentication, @RequestBody Map<String, Object> body) {
        Long buildingId = resolveBuildingId(authentication, body.get("buildingId"));
        BigDecimal baseLimit = body.get("baseLimit") != null ? new BigDecimal(body.get("baseLimit").toString()) : new BigDecimal("10.00");
        BigDecimal baseRate = body.get("baseRate") != null ? new BigDecimal(body.get("baseRate").toString()) : new BigDecimal("15.00");
        BigDecimal extraRate = body.get("extraRate") != null ? new BigDecimal(body.get("extraRate").toString()) : new BigDecimal("25.00");
        LocalDate effectiveFrom = body.get("effectiveFrom") != null ? LocalDate.parse(body.get("effectiveFrom").toString()) : LocalDate.now();

        return ResponseEntity.ok(tariffPlanService.createOrUpdateTariff(buildingId, baseLimit, baseRate, extraRate, effectiveFrom));
    }

    @PostMapping({"/tariff-plans/cycle/{cycleId}/markup", "/tariffs/cycle/{cycleId}/markup", "/api/tariff-plans/cycle/{cycleId}/markup"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<TariffPlan> updateMarkupForCycle(
            @PathVariable("cycleId") Long cycleId,
            @RequestBody Map<String, Object> body
    ) {
        BigDecimal markupPercentage = new BigDecimal("20.00");
        if (body.get("markupPercentage") != null && !body.get("markupPercentage").toString().isEmpty()) {
            markupPercentage = new BigDecimal(body.get("markupPercentage").toString());
        }
        return ResponseEntity.ok(tariffCalculationService.updateMarkupForCycle(cycleId, markupPercentage));
    }

    @GetMapping({"/tariff-plans", "/tariffs", "/tariff-plans/building/{buildingId}", "/tariffs/building/{buildingId}", "/admin/tariff", "/api/admin/tariff", "/api/tariff-plans", "/api/tariff-plans/building/{buildingId}"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<TariffPlan> getLatestTariff(
            Authentication authentication,
            @PathVariable(value = "buildingId", required = false) Long pathBuildingId,
            @RequestParam(value = "buildingId", required = false) Long paramBuildingId,
            @RequestParam(value = "billingCycleId", required = false) Long billingCycleId
    ) {
        Long rawId = pathBuildingId != null ? pathBuildingId : paramBuildingId;
        Long resolvedId = resolveBuildingId(authentication, rawId);
        return ResponseEntity.ok(tariffPlanService.getActiveTariffPlan(resolvedId, billingCycleId));
    }

    @PutMapping({"/tariff-plans", "/tariffs", "/tariff-plans/{id}", "/admin/tariff", "/api/admin/tariff", "/api/tariff-plans/{id}"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<TariffPlan> updateTariff(
            Authentication authentication,
            @PathVariable(value = "id", required = false) Long planId,
            @RequestBody Map<String, Object> body
    ) {
        Long buildingId = resolveBuildingId(authentication, body.get("buildingId"));
        BigDecimal baseLimit = body.get("baseLimit") != null ? new BigDecimal(body.get("baseLimit").toString()) : new BigDecimal("10.00");
        BigDecimal baseRate = body.get("baseRate") != null ? new BigDecimal(body.get("baseRate").toString()) : new BigDecimal("15.00");
        BigDecimal extraRate = body.get("extraRate") != null ? new BigDecimal(body.get("extraRate").toString()) : new BigDecimal("25.00");
        LocalDate effectiveFrom = body.get("effectiveFrom") != null ? LocalDate.parse(body.get("effectiveFrom").toString()) : LocalDate.now();

        if (planId != null) {
            TariffPlan approved = tariffPlanService.approveTariffPlan(planId);
            return ResponseEntity.ok(approved);
        }

        return ResponseEntity.ok(tariffPlanService.createOrUpdateTariff(buildingId, baseLimit, baseRate, extraRate, effectiveFrom));
    }

    @GetMapping({"/tariff-plans/recommendation/{buildingId}/{billingCycleId}", "/tariffs/recommendation/{buildingId}/{billingCycleId}", "/api/tariff-plans/recommendation/{buildingId}/{billingCycleId}"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<TariffRecommendationResponse> getTariffRecommendation(
            @PathVariable("buildingId") Long buildingId,
            @PathVariable("billingCycleId") Long billingCycleId
    ) {
        return ResponseEntity.ok(tariffRecommendationService.getTariffRecommendation(buildingId, billingCycleId));
    }

    @GetMapping({"/tariff-plans/all", "/tariffs/all", "/admin/tariff/all", "/api/admin/tariff/all", "/api/tariff-plans/all"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<List<TariffPlan>> getAllTariffs(Authentication authentication, @RequestParam(required = false) Long buildingId) {
        Long resolvedId = resolveBuildingId(authentication, buildingId);
        return ResponseEntity.ok(tariffPlanService.getAllTariffs(resolvedId));
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
