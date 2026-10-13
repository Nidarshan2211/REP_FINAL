package com.smartwater.billing.service;

import com.smartwater.billing.entity.Invoice;
import com.smartwater.billing.entity.Resident;
import com.smartwater.billing.exception.ResourceNotFoundException;
import com.smartwater.billing.repository.InvoiceRepository;
import com.smartwater.billing.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ResidentRepository residentRepository;

    public List<Invoice> getResidentInvoices(String residentEmail) {
        Resident resident = residentRepository.findByUserEmail(residentEmail)
                .or(() -> residentRepository.findByEmail(residentEmail))
                .orElseThrow(() -> new ResourceNotFoundException("Resident profile not found for " + residentEmail));

        return invoiceRepository.findByResidentResidentIdOrderByCreatedAtDesc(resident.getResidentId());
    }

    public List<Invoice> getCycleInvoices(Long cycleId) {
        return invoiceRepository.findByBillingCycleId(cycleId);
    }
}
