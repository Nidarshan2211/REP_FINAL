package com.smartwater.billing.resident.repository;

import com.smartwater.billing.resident.entity.AwayPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AwayPeriodRepository extends JpaRepository<AwayPeriod, Long> {

    List<AwayPeriod> findByOccupancyOccupancyIdOrderByStartDateDesc(Long occupancyId);

    List<AwayPeriod> findByApartmentApartmentIdOrderByStartDateDesc(Long apartmentId);

    @Query("SELECT a FROM AwayPeriod a WHERE a.occupancy.occupancyId = :occupancyId AND ((a.startDate <= :endDate AND a.endDate >= :startDate))")
    List<AwayPeriod> findOverlappingPeriods(@Param("occupancyId") Long occupancyId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT a FROM AwayPeriod a WHERE a.apartment.apartmentId = :apartmentId AND ((a.startDate <= :endDate AND a.endDate >= :startDate))")
    List<AwayPeriod> findByApartmentAndDateRange(@Param("apartmentId") Long apartmentId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
