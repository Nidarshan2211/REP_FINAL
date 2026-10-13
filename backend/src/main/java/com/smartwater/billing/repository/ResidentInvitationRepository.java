package com.smartwater.billing.repository;

import com.smartwater.billing.entity.ResidentInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResidentInvitationRepository extends JpaRepository<ResidentInvitation, Long> {
    Optional<ResidentInvitation> findByTokenHash(String tokenHash);
    Optional<ResidentInvitation> findTopByResidentEmailAndBuildingBuildingIdAndStatusOrderByCreatedAtDesc(String email, Long buildingId, ResidentInvitation.InvitationStatus status);
    List<ResidentInvitation> findByResidentEmailOrderByCreatedAtDesc(String email);
    List<ResidentInvitation> findByBuildingBuildingIdOrderByCreatedAtDesc(Long buildingId);
}
