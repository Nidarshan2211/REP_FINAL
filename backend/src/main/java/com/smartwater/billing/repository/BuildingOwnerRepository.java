package com.smartwater.billing.repository;

import com.smartwater.billing.entity.BuildingOwner;
import com.smartwater.billing.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuildingOwnerRepository extends JpaRepository<BuildingOwner, Long> {
    Optional<BuildingOwner> findByUser(User user);
    
    @Query("SELECT b FROM BuildingOwner b WHERE (b.user IS NOT NULL AND b.user.userId = :userId) OR LOWER(b.user.email) = LOWER(:email)")
    Optional<BuildingOwner> findByUserIdOrEmail(@Param("userId") Long userId, @Param("email") String email);
    
    List<BuildingOwner> findByApprovalStatus(BuildingOwner.ApprovalStatus status);
    Page<BuildingOwner> findByApprovalStatus(BuildingOwner.ApprovalStatus status, Pageable pageable);
}
