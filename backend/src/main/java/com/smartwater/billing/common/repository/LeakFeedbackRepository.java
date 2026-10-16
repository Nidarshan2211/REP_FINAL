package com.smartwater.billing.common.repository;

import com.smartwater.billing.common.entity.LeakFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeakFeedbackRepository extends JpaRepository<LeakFeedback, Long> {

    List<LeakFeedback> findByUsageUsageIdOrderByCreatedAtDesc(Long usageId);

    List<LeakFeedback> findByApartmentApartmentIdOrderByCreatedAtDesc(Long apartmentId);

    List<LeakFeedback> findByApartmentBuildingBuildingIdOrderByCreatedAtDesc(Long buildingId);

    long countByApartmentApartmentIdAndFeedback(Long apartmentId, String feedback);

    Optional<LeakFeedback> findFirstByUsageUsageIdOrderByCreatedAtDesc(Long usageId);

    boolean existsByUsageUsageIdAndSubmittedByUserUserId(Long usageId, Long userId);
}
