package com.smartwater.billing.controller;

import com.smartwater.billing.dto.BillingDTOs.*;
import com.smartwater.billing.entity.Bill;
import com.smartwater.billing.entity.BillingCycle;
import com.smartwater.billing.entity.Invoice;
import com.smartwater.billing.exception.BadRequestException;
import com.smartwater.billing.service.BillingCycleService;
import com.smartwater.billing.service.BillingService;
import com.smartwater.billing.service.InvoiceService;
import com.smartwater.billing.service.PdfGeneratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class BillController {

    private final BillingService billingService;
    private final BillingCycleService billingCycleService;
    private final InvoiceService invoiceService;
    private final PdfGeneratorService pdfGeneratorService;

    @PostMapping("/bills/generate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<BillResponse> generateBill(
            Authentication authentication,
            @Valid @RequestBody GenerateBillRequest request
    ) {
        return ResponseEntity.ok(billingService.generateBill(authentication.getName(), request));
    }

    @PostMapping("/api/bills/generate/{billingCycleId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<BillingCycle> generateBillsForCycle(@PathVariable("billingCycleId") Long billingCycleId) {
        return ResponseEntity.ok(billingCycleService.finalizeCycle(billingCycleId));
    }

    @GetMapping({"/bills/resident/{residentId}", "/api/bills/resident/{residentId}"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER', 'RESIDENT')")
    public ResponseEntity<List<Invoice>> getResidentInvoicesById(@PathVariable("residentId") Long residentId) {
        return ResponseEntity.ok(invoiceService.getCycleInvoices(residentId));
    }

    @GetMapping({"/bills/cycle/{billingCycleId}", "/api/bills/cycle/{billingCycleId}"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER', 'RESIDENT')")
    public ResponseEntity<List<Invoice>> getInvoicesByCycle(@PathVariable("billingCycleId") Long billingCycleId) {
        return ResponseEntity.ok(invoiceService.getCycleInvoices(billingCycleId));
    }

    @GetMapping({"/bills/owner", "/api/bills/owner", "/api/owner/bills"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<List<BillResponse>> getOwnerBills(Authentication authentication) {
        return ResponseEntity.ok(billingService.getOwnerBills(authentication.getName()));
    }

    @GetMapping({"/invoices", "/api/invoices", "/api/owner/invoices", "/api/admin/invoices"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<InvoiceDashboardSummary> getInvoicesSummary(
            Authentication authentication,
            @RequestParam(value = "buildingId", required = false) Long buildingId,
            @RequestParam(value = "billingCycleId", required = false) Long billingCycleId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String search
    ) {
        return ResponseEntity.ok(billingService.getInvoicesSummary(authentication.getName(), buildingId, billingCycleId, status, search));
    }

    @PutMapping({"/bills/{id}/status", "/invoices/{id}/status", "/admin/billing/invoices/{id}/status", "/api/bills/{id}/status", "/api/invoices/{id}/status"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER')")
    public ResponseEntity<BillResponse> updateBillStatus(
            @PathVariable("id") Long billId,
            @RequestParam(value = "status", required = false) String queryStatus,
            @RequestBody(required = false) java.util.Map<String, String> body
    ) {
        String status = queryStatus;
        if (status == null || status.isBlank()) {
            if (body != null) {
                status = body.getOrDefault("status", "PAID");
            } else {
                status = "PAID";
            }
        }
        return ResponseEntity.ok(billingService.updateBillStatus(billId, status));
    }

    @GetMapping({"/bills/{id}/pdf", "/invoices/{id}/pdf", "/admin/billing/invoices/{id}/pdf", "/api/bills/{id}/pdf", "/api/invoices/{id}/pdf"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BUILDING_OWNER', 'RESIDENT')")
    public ResponseEntity<InputStreamResource> downloadBillPdf(@PathVariable("id") Long billId, Authentication authentication) {
        Bill bill = billingService.getBillById(billId);

        // Security check: Verify authorized access to requested bill
        if (authentication != null) {
            boolean isResident = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_RESIDENT"));
            if (isResident) {
                String currentEmail = authentication.getName();
                String resUserEmail = bill.getResident() != null && bill.getResident().getUser() != null ?
                        bill.getResident().getUser().getEmail() : (bill.getResident() != null ? bill.getResident().getEmail() : "");
                if (!resUserEmail.equalsIgnoreCase(currentEmail.trim()) && (bill.getResident() == null || !currentEmail.equalsIgnoreCase(bill.getResident().getEmail()))) {
                    throw new BadRequestException("Unauthorized access: You are not authorized to download an invoice for another resident!");
                }
            }
        }

        ByteArrayInputStream pdfStream = pdfGeneratorService.generateBillPdf(bill);

        String filename = bill.getStatus() == Bill.BillStatus.PAID ?
                "Water_Bill_Paid_Receipt_SWB_" + billId + ".pdf" :
                "Water_Bill_SWB_" + billId + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + filename);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfStream));
    }
}
