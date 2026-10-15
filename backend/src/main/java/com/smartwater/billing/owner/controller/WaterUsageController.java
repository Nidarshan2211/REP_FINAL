package com.smartwater.billing.owner.controller;

import com.smartwater.billing.common.dto.UsageDTOs.UsageResponse;
import com.smartwater.billing.common.service.WaterUsageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/usage", "/water-usage", "/owner/usage", "/api/usage", "/api/owner/usage"})
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
public class WaterUsageController {

    private final WaterUsageService usageService;

    public WaterUsageController(WaterUsageService usageService) {
        this.usageService = usageService;
    }

    @GetMapping({"/records", "", "/owner"})
    public ResponseEntity<List<UsageResponse>> getRecords(
            Authentication authentication,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) String apartmentNumber,
            @RequestParam(required = false) Long billingCycleId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        return ResponseEntity.ok(usageService.getOwnerUsageRecords(authentication.getName(), buildingId, apartmentNumber, billingCycleId, month, year));
    }

    @GetMapping({"/meters", "/owner/meters", "/api/owner/meters"})
    public ResponseEntity<List<Map<String, Object>>> getMeters(Authentication authentication) {
        return ResponseEntity.ok(usageService.getOwnerMeters(authentication.getName()));
    }
}

