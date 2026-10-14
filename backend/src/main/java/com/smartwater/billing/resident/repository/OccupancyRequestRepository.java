package com.smartwater.billing.resident.repository;

import com.smartwater.billing.resident.entity.OccupancyRequest;
import com.smartwater.billing.resident.entity.OccupancyRequest.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OccupancyRequestRepository extends JpaRepository<OccupancyRequest, Long> {

    @Query("SELECT r FROM OccupancyRequest r WHERE r.apartment.building.owner.ownerId = :ownerId ORDER BY r.requestDate DESC")
    List<OccupancyRequest> findAllByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT r FROM OccupancyRequest r WHERE r.apartment.building.owner.ownerId = :ownerId AND r.status = :status ORDER BY r.requestDate DESC")
    List<OccupancyRequest> findAllByOwnerIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") RequestStatus status);

    @Query("SELECT r FROM OccupancyRequest r WHERE r.resident.residentId = :residentId AND r.status = :status")
    Optional<OccupancyRequest> findActiveRequestByResidentId(@Param("residentId") Long residentId, @Param("status") RequestStatus status);

    @Query("SELECT r FROM OccupancyRequest r WHERE r.apartment.apartmentId = :apartmentId AND r.status = :status")
    Optional<OccupancyRequest> findActiveRequestByApartmentId(@Param("apartmentId") Long apartmentId, @Param("status") RequestStatus status);

    @Query("SELECT r FROM OccupancyRequest r WHERE r.resident.residentId = :residentId ORDER BY r.requestDate DESC")
    List<OccupancyRequest> findAllByResidentId(@Param("residentId") Long residentId);
}
