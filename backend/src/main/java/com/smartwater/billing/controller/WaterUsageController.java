package com.smartwater.billing.controller;

import com.smartwater.billing.dto.UsageDTOs.*;
import com.smartwater.billing.service.WaterUsageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/usage", "/water-usage", "/owner/usage", "/api/usage", "/api/owner/usage"})
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
@RequiredArgsConstructor
public class WaterUsageController {

    private final WaterUsageService usageService;

    @PostMapping({"/record", ""})
    public ResponseEntity<UsageResponse> recordUsage(
            Authentication authentication,
            @Valid @RequestBody RecordUsageRequest request
    ) {
        return ResponseEntity.ok(usageService.recordUsage(authentication.getName(), request));
    }

    @GetMapping({"/records", "", "/owner"})
    public ResponseEntity<List<UsageResponse>> getRecords(Authentication authentication) {
        return ResponseEntity.ok(usageService.getOwnerUsageRecords(authentication.getName()));
    }

    @GetMapping({"/meters", "/owner/meters", "/api/owner/meters"})
    public ResponseEntity<List<Map<String, Object>>> getMeters(Authentication authentication) {
        return ResponseEntity.ok(usageService.getOwnerMeters(authentication.getName()));
    }
}
