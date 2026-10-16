package com.smartwater.billing.common.repository;

import com.smartwater.billing.common.entity.LeakIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeakIncidentRepository extends JpaRepository<LeakIncident, Long> {

    Optional<LeakIncident> findFirstByApartmentApartmentIdAndStatusOrderByOpenedAtDesc(Long apartmentId, String status);

    List<LeakIncident> findByApartmentApartmentIdOrderByOpenedAtDesc(Long apartmentId);

    List<LeakIncident> findByApartmentBuildingBuildingIdAndStatusOrderByOpenedAtDesc(Long buildingId, String status);

    List<LeakIncident> findByApartmentBuildingBuildingIdOrderByOpenedAtDesc(Long buildingId);

    List<LeakIncident> findByStatusOrderByOpenedAtDesc(String status);
}
