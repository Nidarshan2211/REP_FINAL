package com.smartwater.billing.resident.repository;

import com.smartwater.billing.resident.entity.ApartmentOccupancy;
import com.smartwater.billing.resident.entity.ApartmentOccupancy.OccupancyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApartmentOccupancyRepository extends JpaRepository<ApartmentOccupancy, Long> {

    Optional<ApartmentOccupancy> findFirstByApartmentApartmentIdAndStatus(Long apartmentId, OccupancyStatus status);

    Optional<ApartmentOccupancy> findFirstByResidentResidentIdAndStatus(Long residentId, OccupancyStatus status);

    List<ApartmentOccupancy> findByResidentResidentIdOrderByStartDateDesc(Long residentId);

    List<ApartmentOccupancy> findByApartmentApartmentIdOrderByStartDateDesc(Long apartmentId);

    List<ApartmentOccupancy> findByApartmentBuildingBuildingIdAndStatus(Long buildingId, OccupancyStatus status);

    List<ApartmentOccupancy> findByApartmentBuildingOwnerOwnerIdAndStatus(Long ownerId, OccupancyStatus status);

    @Query("SELECT o FROM ApartmentOccupancy o WHERE ((o.resident.user IS NOT NULL AND LOWER(o.resident.user.email) = LOWER(:email)) OR LOWER(o.resident.email) = LOWER(:email)) AND o.status = :status")
    Optional<ApartmentOccupancy> findActiveByResidentEmail(@Param("email") String email, @Param("status") OccupancyStatus status);

    @Query("SELECT o FROM ApartmentOccupancy o WHERE (o.resident.user IS NOT NULL AND LOWER(o.resident.user.email) = LOWER(:email)) OR LOWER(o.resident.email) = LOWER(:email) ORDER BY o.startDate DESC")
    List<ApartmentOccupancy> findAllByResidentEmail(@Param("email") String email);

    boolean existsByApartmentApartmentIdAndStatus(Long apartmentId, OccupancyStatus status);
}
