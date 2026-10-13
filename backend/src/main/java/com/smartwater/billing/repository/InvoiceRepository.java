package com.smartwater.billing.repository;

import com.smartwater.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByResidentResidentIdOrderByCreatedAtDesc(Long residentId);
    List<Invoice> findByBillingCycleId(Long billingCycleId);
    Optional<Invoice> findTopByResidentResidentIdOrderByCreatedAtDesc(Long residentId);
    Optional<Invoice> findByBillingCycleIdAndResidentResidentId(Long billingCycleId, Long residentId);
}
