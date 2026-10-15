package com.smartwater.billing.owner.controller;

import com.smartwater.billing.owner.dto.BulkBillingDTOs.BulkImportResponse;
import com.smartwater.billing.owner.dto.BulkBillingDTOs.BulkValidationResponse;
import com.smartwater.billing.owner.dto.BulkBillingDTOs.CycleReadinessDTO;
import com.smartwater.billing.owner.service.BulkBillingService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping({"/bulk-billing", "/api/bulk-billing"})
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
public class BulkBillingController {

    private final BulkBillingService bulkBillingService;

    public BulkBillingController(BulkBillingService bulkBillingService) {
        this.bulkBillingService = bulkBillingService;
    }


    @GetMapping("/readiness")
    public ResponseEntity<CycleReadinessDTO> getReadiness(
            Authentication authentication,
            @RequestParam("buildingId") Long buildingId,
            @RequestParam("billingCycleId") Long cycleId
    ) {
        return ResponseEntity.ok(bulkBillingService.getCycleReadiness(authentication.getName(), buildingId, cycleId));
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate(
            Authentication authentication,
            @RequestParam("buildingId") Long buildingId,
            @RequestParam("billingCycleId") Long cycleId
    ) {
        byte[] csvData = bulkBillingService.generateTemplate(authentication.getName(), buildingId, cycleId);

        String filename = "Meter_Readings_Template_Building_" + buildingId + "_Cycle_" + cycleId + "_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }

    @PostMapping("/validate")
    public ResponseEntity<BulkValidationResponse> validateCsv(
            Authentication authentication,
            @RequestParam("buildingId") String rawBuildingId,
            @RequestParam("billingCycleId") String rawCycleId,
            @RequestParam("file") MultipartFile file
    ) {
        Long buildingId = parseFirstLong(rawBuildingId);
        Long cycleId = parseFirstLong(rawCycleId);
        return ResponseEntity.ok(bulkBillingService.validateCsv(authentication.getName(), buildingId, cycleId, file));
    }

    @PostMapping("/error-report")
    public ResponseEntity<byte[]> downloadErrorReport(@RequestBody BulkValidationResponse response) {
        byte[] csvData = bulkBillingService.generateErrorReport(response);

        String filename = "Meter_Readings_Errors_Building_" + response.getBuildingId() + "_Cycle_" + response.getBillingCycleId() + ".csv";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }

    @PostMapping("/import-readings")
    public ResponseEntity<BulkImportResponse> importReadings(
            Authentication authentication,
            @RequestParam("buildingId") String rawBuildingId,
            @RequestParam("billingCycleId") String rawCycleId,
            @RequestParam("file") MultipartFile file
    ) {
        Long buildingId = parseFirstLong(rawBuildingId);
        Long cycleId = parseFirstLong(rawCycleId);
        return ResponseEntity.ok(bulkBillingService.importReadings(authentication.getName(), buildingId, cycleId, file));
    }

    @PostMapping("/generate")
    public ResponseEntity<BulkImportResponse> generateBills(
            Authentication authentication,
            @RequestParam("buildingId") String rawBuildingId,
            @RequestParam("billingCycleId") String rawCycleId,
            @RequestParam("file") MultipartFile file
    ) {
        Long buildingId = parseFirstLong(rawBuildingId);
        Long cycleId = parseFirstLong(rawCycleId);
        return ResponseEntity.ok(bulkBillingService.generateBills(authentication.getName(), buildingId, cycleId, file));
    }

    private Long parseFirstLong(String val) {
        if (val == null || val.isBlank()) return null;
        if (val.contains(",")) {
            val = val.split(",")[0].trim();
        }
        return Long.parseLong(val.trim());
    }
}
