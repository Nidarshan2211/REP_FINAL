package com.smartwater.billing.owner.repository;

import com.smartwater.billing.owner.entity.Apartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, Long> {

    List<Apartment> findByBuildingBuildingIdOrderByFloorNumberAscRoomNumberAsc(Long buildingId);

    List<Apartment> findByBuildingBuildingId(Long buildingId);

    List<Apartment> findByBuildingOwnerOwnerIdOrderByBuildingBuildingNameAscFloorNumberAscRoomNumberAsc(Long ownerId);

    Optional<Apartment> findByBuildingBuildingIdAndRoomNumber(Long buildingId, String roomNumber);

    boolean existsByBuildingBuildingIdAndRoomNumber(Long buildingId, String roomNumber);

    @Query("SELECT a FROM Apartment a WHERE a.building.buildingId = :buildingId AND a.floorNumber = :floorNumber ORDER BY a.roomNumber ASC")
    List<Apartment> findByBuildingAndFloor(@Param("buildingId") Long buildingId, @Param("floorNumber") Integer floorNumber);
}
