package com.smartwater.billing.common.service;

import com.smartwater.billing.common.entity.Invoice;
import com.smartwater.billing.resident.entity.Resident;
import com.smartwater.billing.common.exception.ResourceNotFoundException;
import com.smartwater.billing.common.repository.InvoiceRepository;
import com.smartwater.billing.resident.repository.ResidentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ResidentRepository residentRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, ResidentRepository residentRepository) {
        this.invoiceRepository = invoiceRepository;
        this.residentRepository = residentRepository;
    }


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
