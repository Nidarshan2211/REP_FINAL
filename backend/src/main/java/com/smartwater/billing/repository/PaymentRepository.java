package com.smartwater.billing.repository;

import com.smartwater.billing.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBillBillId(Long billId);
    List<Payment> findByBillResidentResidentIdOrderByPaymentDateDesc(Long residentId);

    @Query("SELECT p FROM Payment p WHERE p.bill.resident.building.owner.ownerId = :ownerId ORDER BY p.paymentDate DESC")
    List<Payment> findByOwnerId(@Param("ownerId") Long ownerId);
}
