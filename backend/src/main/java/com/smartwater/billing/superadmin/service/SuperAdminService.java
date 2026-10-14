package com.smartwater.billing.superadmin.service;

import com.smartwater.billing.common.dto.DashboardDTOs.*;
import com.smartwater.billing.owner.entity.BuildingOwner;
import com.smartwater.billing.common.entity.User;
import com.smartwater.billing.common.exception.ResourceNotFoundException;
import com.smartwater.billing.common.repository.*;
import com.smartwater.billing.owner.repository.*;
import com.smartwater.billing.resident.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SuperAdminService {

    private final BuildingOwnerRepository ownerRepository;
    private final BuildingRepository buildingRepository;
    private final ResidentRepository residentRepository;
    private final BillRepository billRepository;
    private final UserRepository userRepository;

    public SuperAdminService(BuildingOwnerRepository ownerRepository, BuildingRepository buildingRepository, ResidentRepository residentRepository, BillRepository billRepository, UserRepository userRepository) {
        this.ownerRepository = ownerRepository;
        this.buildingRepository = buildingRepository;
        this.residentRepository = residentRepository;
        this.billRepository = billRepository;
        this.userRepository = userRepository;
    }


    public AdminDashboardStats getDashboardStats() {
        long totalOwners = ownerRepository.count();
        long pendingOwners = ownerRepository.findByApprovalStatus(BuildingOwner.ApprovalStatus.PENDING).size();
        long totalBuildings = buildingRepository.count();
        long totalResidents = residentRepository.count();
        Double totalRev = billRepository.getTotalSystemRevenue();
        double totalSystemRevenue = totalRev != null ? totalRev : 0.0;

        List<BuildingOwnerDTO> recentOwners = ownerRepository.findAll().stream()
                .limit(5)
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
        user.setStatus(User.UserStatus.APPROVED);
        userRepository.save(user);
        ownerRepository.save(owner);

        return convertToOwnerDTO(owner);
    }

    @Transactional
    public BuildingOwnerDTO rejectOwner(Long ownerId) {
        BuildingOwner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Building Owner not found with id: " + ownerId));

        owner.setApprovalStatus(BuildingOwner.ApprovalStatus.REJECTED);
        User user = owner.getUser();
        user.setStatus(User.UserStatus.REJECTED);
        userRepository.save(user);
        ownerRepository.save(owner);

        return convertToOwnerDTO(owner);
    }

    private BuildingOwnerDTO convertToOwnerDTO(BuildingOwner owner) {
        int totalBuildings = buildingRepository.findByOwner(owner).size();
        return BuildingOwnerDTO.builder()
                .ownerId(owner.getOwnerId())
                .userId(owner.getUser().getUserId())
                .name(owner.getUser().getName())
                .email(owner.getUser().getEmail())
                .phone(owner.getPhone())
                .approvalStatus(owner.getApprovalStatus().name())
                .totalBuildings(totalBuildings)
                .createdAt(owner.getCreatedAt() != null ? owner.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "")
                .build();
    }
}
