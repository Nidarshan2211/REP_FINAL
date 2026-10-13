package com.smartwater.billing.repository;

import com.smartwater.billing.entity.Building;
import com.smartwater.billing.entity.Resident;
import com.smartwater.billing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Long> {
    Optional<Resident> findByUser(User user);
    
    @Query("SELECT r FROM Resident r WHERE (r.user IS NOT NULL AND r.user.userId = :userId) OR LOWER(r.email) = LOWER(:email)")
    Optional<Resident> findByUserIdOrEmail(@Param("userId") Long userId, @Param("email") String email);

    Optional<Resident> findByEmail(String email);
    Optional<Resident> findByEmailIgnoreCase(String email);
    Optional<Resident> findByUserEmail(String email);
    Optional<Resident> findByInvitationToken(String token);
    Optional<Resident> findByTokenHash(String tokenHash);
    List<Resident> findByBuilding(Building building);
    List<Resident> findByBuildingBuildingId(Long buildingId);
    List<Resident> findByBuildingOwnerOwnerId(Long ownerId);

    @Query("SELECT r FROM Resident r WHERE r.building.owner.ownerId = :ownerId " +
           "AND (:query IS NULL OR :query = '' OR LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(r.email) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:status IS NULL OR r.accountStatus = :status)")
    List<Resident> searchResidentsForOwner(
            @Param("ownerId") Long ownerId,
            @Param("query") String query,
            @Param("status") Resident.AccountStatus status
    );
}
