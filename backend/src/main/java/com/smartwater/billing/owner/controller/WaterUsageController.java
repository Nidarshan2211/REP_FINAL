package com.smartwater.billing.owner.controller;

import com.smartwater.billing.owner.dto.BulkBillingDTOs.BulkImportResponse;
import com.smartwater.billing.common.dto.UsageDTOs.*;
import com.smartwater.billing.owner.service.BulkBillingService;
import com.smartwater.billing.common.service.WaterUsageService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/usage", "/water-usage", "/owner/usage", "/api/usage", "/api/owner/usage"})
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
public class WaterUsageController {

    private final WaterUsageService usageService;
    private final BulkBillingService bulkBillingService;

    public WaterUsageController(WaterUsageService usageService, BulkBillingService bulkBillingService) {
        this.usageService = usageService;
        this.bulkBillingService = bulkBillingService;
    }


    @PostMapping({"/record", ""})
    public ResponseEntity<UsageResponse> recordUsage(
            Authentication authentication,
            @Valid @RequestBody RecordUsageRequest request
    ) {
        return ResponseEntity.ok(usageService.recordUsage(authentication.getName(), request));
    }

    @PostMapping(value = {"/bulk-csv", "/upload-csv"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BulkImportResponse> uploadUsageCsv(
            Authentication authentication,
            @RequestParam("buildingId") Long buildingId,
            @RequestParam("billingCycleId") Long cycleId,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(bulkBillingService.importReadings(authentication.getName(), buildingId, cycleId, file));
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
