package com.smartwater.billing.owner.service;

import com.smartwater.billing.owner.dto.BuildingDTOs.*;
import com.smartwater.billing.common.dto.DashboardDTOs.OwnerDashboardStats;
import com.smartwater.billing.owner.entity.Building;
import com.smartwater.billing.owner.entity.BuildingOwner;
import com.smartwater.billing.common.entity.User;
import com.smartwater.billing.common.exception.ResourceNotFoundException;
import com.smartwater.billing.common.repository.*;
import com.smartwater.billing.owner.repository.*;
import com.smartwater.billing.resident.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BuildingOwnerService {

    private final BuildingOwnerRepository ownerRepository;
    private final BuildingRepository buildingRepository;
    private final ResidentRepository residentRepository;
    private final BillRepository billRepository;
    private final UserRepository userRepository;

    public BuildingOwnerService(BuildingOwnerRepository ownerRepository, BuildingRepository buildingRepository, ResidentRepository residentRepository, BillRepository billRepository, UserRepository userRepository) {
        this.ownerRepository = ownerRepository;
        this.buildingRepository = buildingRepository;
        this.residentRepository = residentRepository;
        this.billRepository = billRepository;
        this.userRepository = userRepository;
    }


    public BuildingOwner getOwnerByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return ownerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Owner profile not found"));
    }

    public OwnerDashboardStats getOwnerDashboardStats(String email) {
        BuildingOwner owner = getOwnerByUserEmail(email);
        List<Building> buildings = buildingRepository.findByOwner(owner);
        long totalResidents = residentRepository.findByBuildingOwnerOwnerId(owner.getOwnerId()).size();
        long pendingInvitations = residentRepository.findByBuildingOwnerOwnerId(owner.getOwnerId()).stream()
                .filter(r -> r.getInvitationStatus() == com.smartwater.billing.resident.entity.Resident.InvitationStatus.PENDING)
                .count();
        long totalBills = billRepository.findByOwnerId(owner.getOwnerId()).size();
        Double totalRev = billRepository.getTotalRevenueByOwner(owner.getOwnerId());
        double totalRevenue = totalRev != null ? totalRev : 0.0;

        return OwnerDashboardStats.builder()
                .totalBuildings(buildings.size())
                .totalResidents(totalResidents)
                .pendingInvitations(pendingInvitations)
                .totalBillsGenerated(totalBills)
                .totalRevenue(totalRevenue)
                .build();
    }

    public List<BuildingResponse> getOwnerBuildings(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        if (user.getRole() == User.Role.SUPER_ADMIN) {
            return buildingRepository.findAll().stream()
                    .map(this::convertToBuildingResponse)
                    .collect(Collectors.toList());
        }
        BuildingOwner owner = ownerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Owner profile not found"));
        return buildingRepository.findByOwner(owner).stream()
                .map(this::convertToBuildingResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BuildingResponse createBuilding(String email, CreateBuildingRequest request) {
        BuildingOwner owner = getOwnerByUserEmail(email);
        Building building = Building.builder()
                .owner(owner)
                .buildingName(request.getBuildingName())
                .buildingNumber(request.getBuildingNumber())
                .totalFlats(request.getTotalFlats())
                .location(request.getLocation())
                .build();
        buildingRepository.save(building);
        return convertToBuildingResponse(building);
    }

    private BuildingResponse convertToBuildingResponse(Building building) {
        return BuildingResponse.builder()
                .buildingId(building.getBuildingId())
                .ownerId(building.getOwner().getOwnerId())
                .ownerName(building.getOwner().getUser().getName())
                .buildingName(building.getBuildingName())
                .buildingNumber(building.getBuildingNumber())
                .totalFlats(building.getTotalFlats())
                .location(building.getLocation())
                .createdAt(building.getCreatedAt())
                .build();
    }
}
