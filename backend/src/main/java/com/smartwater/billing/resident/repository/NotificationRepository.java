package com.smartwater.billing.resident.repository;


import com.smartwater.billing.common.entity.*;
import com.smartwater.billing.owner.entity.*;
import com.smartwater.billing.resident.entity.*;
import com.smartwater.billing.resident.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByResidentResidentIdOrderByCreatedAtDesc(Long residentId);
    List<Notification> findByResidentResidentIdAndIsReadFalse(Long residentId);
}
