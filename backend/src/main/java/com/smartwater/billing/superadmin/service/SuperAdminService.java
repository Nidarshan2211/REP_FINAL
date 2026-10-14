package com.smartwater.billing.superadmin.service;

import com.smartwater.billing.common.dto.DashboardDTOs.*;
import com.smartwater.billing.common.entity.User;
import com.smartwater.billing.common.exception.ResourceNotFoundException;
import com.smartwater.billing.common.repository.*;
import com.smartwater.billing.owner.entity.Building;
import com.smartwater.billing.owner.entity.BuildingOwner;
import com.smartwater.billing.owner.repository.*;
import com.smartwater.billing.owner.service.BuildingOwnerService;
import com.smartwater.billing.resident.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SuperAdminService {

    private final BuildingOwnerRepository ownerRepository;
    private final BuildingRepository buildingRepository;
    private final ResidentRepository residentRepository;
    private final BillRepository billRepository;
    private final UserRepository userRepository;
    private final BuildingOwnerService buildingOwnerService;

    @PersistenceContext
    private EntityManager entityManager;

    public SuperAdminService(
            BuildingOwnerRepository ownerRepository,
            BuildingRepository buildingRepository,
            ResidentRepository residentRepository,
            BillRepository billRepository,
            UserRepository userRepository,
            BuildingOwnerService buildingOwnerService
    ) {
        this.ownerRepository = ownerRepository;
        this.buildingRepository = buildingRepository;
        this.residentRepository = residentRepository;
        this.billRepository = billRepository;
        this.userRepository = userRepository;
        this.buildingOwnerService = buildingOwnerService;
    }

    public AdminDashboardStats getDashboardStats() {
        long totalOwners = ownerRepository.count();
        long pendingOwners = ownerRepository.findByApprovalStatus(BuildingOwner.ApprovalStatus.PENDING).size();
        long totalBuildings = buildingRepository.count();
        long totalResidents = residentRepository.count();
        Double totalRev = billRepository.getTotalSystemRevenue();
        double totalSystemRevenue = totalRev != null ? totalRev : 0.0;

        List<BuildingOwnerDTO> recentOwners = ownerRepository.findAll().stream()
                .map(this::convertToOwnerDTO)
                .collect(Collectors.toList());

        return AdminDashboardStats.builder()
                .totalOwners(totalOwners)
                .pendingOwners(pendingOwners)
                .totalBuildings(totalBuildings)
                .totalResidents(totalResidents)
                .totalSystemRevenue(totalSystemRevenue)
                .recentOwners(recentOwners)
                .build();
    }

    public List<BuildingOwnerDTO> getAllOwners() {
        return ownerRepository.findAll().stream()
                .map(this::convertToOwnerDTO)
                .collect(Collectors.toList());
    }

    public List<BuildingOwnerDTO> getPendingOwners() {
        return ownerRepository.findByApprovalStatus(BuildingOwner.ApprovalStatus.PENDING).stream()
                .map(this::convertToOwnerDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BuildingOwnerDTO approveOwner(Long ownerId) {
        BuildingOwner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Building Owner not found with id: " + ownerId));

        owner.setApprovalStatus(BuildingOwner.ApprovalStatus.APPROVED);
        User user = owner.getUser();
        if (user != null) {
            user.setStatus(User.UserStatus.APPROVED);
            userRepository.save(user);
        }
        ownerRepository.save(owner);

        return convertToOwnerDTO(owner);
    }

    @Transactional
    public BuildingOwnerDTO rejectOwner(Long ownerId) {
        BuildingOwner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Building Owner not found with id: " + ownerId));

        owner.setApprovalStatus(BuildingOwner.ApprovalStatus.REJECTED);
        User user = owner.getUser();
        if (user != null) {
            user.setStatus(User.UserStatus.REJECTED);
            userRepository.save(user);
        }
        ownerRepository.save(owner);

        return convertToOwnerDTO(owner);
    }

    @Transactional
    public void deleteOwner(Long ownerId) {
        BuildingOwner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Building Owner not found with id: " + ownerId));

        // 1. Log REMOVED event into owner_history table before deletion
        String email = owner.getUser() != null ? owner.getUser().getEmail() : "N/A";
        String phone = owner.getPhone() != null ? owner.getPhone() : "";
        try {
            entityManager.createNativeQuery(
                    "INSERT INTO owner_history (owner_id, owner_name, email, phone, event_type, details, actor, timestamp) " +
                    "VALUES (:ownerId, :name, :email, :phone, 'REMOVED', 'Building owner removed from platform by Super Admin.', 'System Super Admin', NOW())"
            ).setParameter("ownerId", ownerId)
             .setParameter("name", owner.getName())
             .setParameter("email", email)
             .setParameter("phone", phone)
             .executeUpdate();
        } catch (Exception ignored) {}

        // 2. Cascade delete all buildings owned by this owner
        List<Building> buildings = buildingRepository.findByOwner(owner);
        for (Building b : buildings) {
            buildingOwnerService.deleteBuilding("admin@smartwater.com", b.getBuildingId());
        }

        // 3. Delete resident invitations issued by this owner
        entityManager.createNativeQuery("DELETE FROM resident_invitations WHERE invited_by_id = :ownerId")
                .setParameter("ownerId", ownerId)
                .executeUpdate();

        // 4. Delete building owner profile
        Long userId = owner.getUser() != null ? owner.getUser().getUserId() : null;
        entityManager.createNativeQuery("DELETE FROM building_owners WHERE owner_id = :ownerId")
                .setParameter("ownerId", ownerId)
                .executeUpdate();

        // 5. Delete user account if not super admin
        if (userId != null) {
            entityManager.createNativeQuery("DELETE FROM users WHERE user_id = :userId AND role != 'SUPER_ADMIN'")
                    .setParameter("userId", userId)
                    .executeUpdate();
        }
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<SystemHistoryItemDTO> getSystemHistory() {
        List<SystemHistoryItemDTO> items = new ArrayList<>();

        // 1. Current / Joined Building Owners
        for (BuildingOwner owner : ownerRepository.findAll()) {
            String ts = owner.getCreatedAt() != null ? owner.getCreatedAt().toString() : "2026-10-14T00:00:00";
            String email = owner.getUser() != null ? owner.getUser().getEmail() : "N/A";
            String phone = owner.getPhone() != null && !owner.getPhone().isEmpty() ? owner.getPhone() : "Not provided";
            String status = owner.getApprovalStatus() == BuildingOwner.ApprovalStatus.APPROVED ? "JOINED" : owner.getApprovalStatus().name();
            items.add(new SystemHistoryItemDTO(
                    "JOIN-" + owner.getOwnerId(),
                    "JOINED",
                    "Building Owner Joined: " + owner.getName(),
                    "Owner account registered with email " + email + " and phone " + phone + " (Approval Status: " + owner.getApprovalStatus() + ")",
                    ts,
                    status,
                    owner.getName()
            ));
        }

        // 2. Removed / Left Building Owners from owner_history table
        try {
            List<Object[]> rows = entityManager.createNativeQuery(
                    "SELECT id, owner_id, owner_name, email, phone, event_type, details, actor, timestamp FROM owner_history ORDER BY timestamp DESC"
            ).getResultList();

            for (Object[] r : rows) {
                Long id = ((Number) r[0]).longValue();
                String ownerName = (String) r[2];
                String email = (String) r[3];
                String phone = (String) r[4];
                String eventType = (String) r[5];
                String details = (String) r[6];
                String actor = (String) r[7];
                String ts = r[8] != null ? r[8].toString() : "2026-10-14T00:00:00";

                items.add(new SystemHistoryItemDTO(
                        "HIST-" + id,
                        eventType != null ? eventType : "REMOVED",
                        "Building Owner Removed: " + ownerName,
                        details != null ? details : "Building owner removed from platform. Contact: " + email + " / " + phone,
                        ts,
                        "REMOVED",
                        actor != null ? actor : "System Super Admin"
                ));
            }
        } catch (Exception ignored) {}

        // Sort by timestamp descending
        items.sort((a, b) -> {
            if (a.getTimestamp() == null) return 1;
            if (b.getTimestamp() == null) return -1;
            return b.getTimestamp().compareTo(a.getTimestamp());
        });

        return items;
    }

    private BuildingOwnerDTO convertToOwnerDTO(BuildingOwner owner) {
        int totalBuildings = buildingRepository.findByOwner(owner).size();
        return BuildingOwnerDTO.builder()
                .ownerId(owner.getOwnerId())
                .userId(owner.getUser() != null ? owner.getUser().getUserId() : null)
                .name(owner.getName())
                .email(owner.getUser() != null ? owner.getUser().getEmail() : "")
                .phone(owner.getPhone())
                .approvalStatus(owner.getApprovalStatus().name())
                .totalBuildings(totalBuildings)
                .createdAt(owner.getCreatedAt() != null ? owner.getCreatedAt().toString() : "")
                .build();
    }
}
