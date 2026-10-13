package com.smartwater.billing.repository;

import com.smartwater.billing.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByResidentResidentIdOrderByCreatedAtDesc(Long residentId);
    List<Notification> findByResidentResidentIdAndIsReadFalse(Long residentId);
}
