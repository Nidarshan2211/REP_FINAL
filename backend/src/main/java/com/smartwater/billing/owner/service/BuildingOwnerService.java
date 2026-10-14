package com.smartwater.billing.owner.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.smartwater.billing.owner.dto.BuildingDTOs.*;
import com.smartwater.billing.common.dto.DashboardDTOs.OwnerDashboardStats;
import com.smartwater.billing.owner.entity.Apartment;
import com.smartwater.billing.owner.entity.Building;
import com.smartwater.billing.owner.entity.BuildingOwner;
import com.smartwater.billing.common.entity.User;
import com.smartwater.billing.common.entity.WaterMeter;
import com.smartwater.billing.common.exception.BadRequestException;
import com.smartwater.billing.common.exception.ResourceNotFoundException;
import com.smartwater.billing.common.repository.*;
import com.smartwater.billing.owner.repository.*;
import com.smartwater.billing.resident.entity.ApartmentOccupancy;
import com.smartwater.billing.resident.entity.ApartmentOccupancy.OccupancyStatus;
import com.smartwater.billing.resident.entity.OccupancyRequest;
import com.smartwater.billing.resident.entity.Resident;
import com.smartwater.billing.resident.entity.ResidentInvitation;
import com.smartwater.billing.resident.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BuildingOwnerService {

    @PersistenceContext
    private EntityManager entityManager;

    private final BuildingOwnerRepository ownerRepository;
    private final BuildingRepository buildingRepository;
    private final ResidentRepository residentRepository;
    private final BillRepository billRepository;
    private final UserRepository userRepository;
    private final ApartmentRepository apartmentRepository;
    private final ApartmentOccupancyRepository occupancyRepository;
    private final OccupancyRequestRepository occupancyRequestRepository;
    private final WaterMeterRepository meterRepository;
    private final ResidentInvitationRepository invitationRepository;

    public BuildingOwnerService(
            BuildingOwnerRepository ownerRepository,
            BuildingRepository buildingRepository,
            ResidentRepository residentRepository,
            BillRepository billRepository,
            UserRepository userRepository,
            ApartmentRepository apartmentRepository,
            ApartmentOccupancyRepository occupancyRepository,
            OccupancyRequestRepository occupancyRequestRepository,
            WaterMeterRepository meterRepository,
            ResidentInvitationRepository invitationRepository
    ) {
        this.ownerRepository = ownerRepository;
        this.buildingRepository = buildingRepository;
        this.residentRepository = residentRepository;
        this.billRepository = billRepository;
        this.userRepository = userRepository;
        this.apartmentRepository = apartmentRepository;
        this.occupancyRepository = occupancyRepository;
        this.occupancyRequestRepository = occupancyRequestRepository;
        this.meterRepository = meterRepository;
        this.invitationRepository = invitationRepository;
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

    @Transactional
    public void deleteBuilding(String email, Long buildingId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + buildingId));

        if (user.getRole() != User.Role.SUPER_ADMIN) {
            BuildingOwner owner = ownerRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Owner profile not found"));
            if (!building.getOwner().getOwnerId().equals(owner.getOwnerId())) {
                throw new BadRequestException("Unauthorized access: You do not own building '" + building.getBuildingName() + "'!");
            }
        }

        // Clean up all related tables in foreign key order:
        // 1. Occupancy requests
        entityManager.createNativeQuery(
                "DELETE FROM occupancy_requests WHERE apartment_id IN (SELECT a.apartment_id FROM (SELECT apartment_id FROM apartments WHERE building_id = :bldgId) a)"
        ).setParameter("bldgId", buildingId).executeUpdate();

        // 2. Resident invitations
        entityManager.createNativeQuery(
                "DELETE FROM resident_invitations WHERE building_id = :bldgId OR apartment_id IN (SELECT a.apartment_id FROM (SELECT apartment_id FROM apartments WHERE building_id = :bldgId) a)"
        ).setParameter("bldgId", buildingId).executeUpdate();

        // 3. Apartment occupancies
        entityManager.createNativeQuery(
                "DELETE FROM apartment_occupancies WHERE apartment_id IN (SELECT a.apartment_id FROM (SELECT apartment_id FROM apartments WHERE building_id = :bldgId) a)"
        ).setParameter("bldgId", buildingId).executeUpdate();

        // 4. Water usage
        entityManager.createNativeQuery(
                "DELETE FROM water_usage WHERE meter_id IN (SELECT m.meter_id FROM (SELECT meter_id FROM water_meters WHERE building_id = :bldgId) m)"
        ).setParameter("bldgId", buildingId).executeUpdate();

        // 5. Water meters
        entityManager.createNativeQuery(
                "DELETE FROM water_meters WHERE building_id = :bldgId"
        ).setParameter("bldgId", buildingId).executeUpdate();

        // 5. Payments (must be deleted before bills)
        entityManager.createNativeQuery(
                "DELETE FROM payments WHERE bill_id IN (" +
                "  SELECT b.bill_id FROM (SELECT bill_id FROM bills WHERE billing_cycle_id IN (SELECT id FROM billing_cycles WHERE building_id = :bldgId) " +
                "  OR resident_id IN (SELECT resident_id FROM residents WHERE building_id = :bldgId)) b)"
        ).setParameter("bldgId", buildingId).executeUpdate();

        // 6. Invoices (must be deleted before billing_cycles and residents)
        entityManager.createNativeQuery(
                "DELETE FROM invoices WHERE billing_cycle_id IN (SELECT b.id FROM (SELECT id FROM billing_cycles WHERE building_id = :bldgId) b) " +
                "OR resident_id IN (SELECT r.resident_id FROM (SELECT resident_id FROM residents WHERE building_id = :bldgId) r)"
        ).setParameter("bldgId", buildingId).executeUpdate();

        // 7. Bills (must be deleted before billing_cycles and residents)
        entityManager.createNativeQuery(
                "DELETE FROM bills WHERE billing_cycle_id IN (SELECT b.id FROM (SELECT id FROM billing_cycles WHERE building_id = :bldgId) b) " +
                "OR resident_id IN (SELECT r.resident_id FROM (SELECT resident_id FROM residents WHERE building_id = :bldgId) r)"
        ).setParameter("bldgId", buildingId).executeUpdate();

        // 8. Notifications
        entityManager.createNativeQuery(
                "DELETE FROM notifications WHERE resident_id IN (SELECT r.resident_id FROM (SELECT resident_id FROM residents WHERE building_id = :bldgId) r)"
        ).setParameter("bldgId", buildingId).executeUpdate();

        // 9. Water meters
        entityManager.createNativeQuery(
                "DELETE FROM water_meters WHERE building_id = :bldgId"
        ).setParameter("bldgId", buildingId).executeUpdate();

        // 10. Tariff plans, water purchases, billing cycles
        entityManager.createNativeQuery("DELETE FROM tariff_plans WHERE building_id = :bldgId").setParameter("bldgId", buildingId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM water_purchases WHERE building_id = :bldgId").setParameter("bldgId", buildingId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM billing_cycles WHERE building_id = :bldgId").setParameter("bldgId", buildingId).executeUpdate();

        // 11. Residents
        entityManager.createNativeQuery(
                "DELETE FROM residents WHERE building_id = :bldgId"
        ).setParameter("bldgId", buildingId).executeUpdate();

        // 12. Apartments
        entityManager.createNativeQuery(
                "DELETE FROM apartments WHERE building_id = :bldgId"
        ).setParameter("bldgId", buildingId).executeUpdate();

        // 13. Building
        entityManager.createNativeQuery(
                "DELETE FROM buildings WHERE building_id = :bldgId"
        ).setParameter("bldgId", buildingId).executeUpdate();
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
