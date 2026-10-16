package com.smartwater.billing.resident.repository;

import com.smartwater.billing.resident.entity.HouseholdSizeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HouseholdSizeHistoryRepository extends JpaRepository<HouseholdSizeHistory, Long> {

    List<HouseholdSizeHistory> findByOccupancyOccupancyIdOrderByEffectiveFromDesc(Long occupancyId);

    List<HouseholdSizeHistory> findByApartmentApartmentIdOrderByEffectiveFromDesc(Long apartmentId);

    Optional<HouseholdSizeHistory> findFirstByOccupancyOccupancyIdAndStatusOrderByEffectiveFromDesc(Long occupancyId, String status);

    Optional<HouseholdSizeHistory> findFirstByApartmentApartmentIdAndStatusOrderByEffectiveFromDesc(Long apartmentId, String status);

    @Query("SELECT h FROM HouseholdSizeHistory h WHERE h.apartment.apartmentId = :apartmentId AND h.status = 'ACTIVE' AND h.effectiveFrom <= :date AND (h.effectiveTo IS NULL OR h.effectiveTo >= :date) ORDER BY h.effectiveFrom DESC")
    List<HouseholdSizeHistory> findActiveByApartmentAndDate(@Param("apartmentId") Long apartmentId, @Param("date") LocalDate date);

    @Query("SELECT h FROM HouseholdSizeHistory h WHERE h.occupancy.occupancyId = :occupancyId AND h.status = 'ACTIVE' AND h.effectiveFrom <= :date AND (h.effectiveTo IS NULL OR h.effectiveTo >= :date) ORDER BY h.effectiveFrom DESC")
    List<HouseholdSizeHistory> findActiveByOccupancyAndDate(@Param("occupancyId") Long occupancyId, @Param("date") LocalDate date);
}
