package com.smartwater.billing.owner.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.smartwater.billing.owner.dto.ApartmentDTOs.*;
import com.smartwater.billing.resident.dto.OccupancyDTOs.AvailableApartmentResponse;
import com.smartwater.billing.owner.entity.Apartment;
import com.smartwater.billing.owner.entity.Building;
import com.smartwater.billing.owner.entity.BuildingOwner;
import com.smartwater.billing.resident.entity.ApartmentOccupancy;
import com.smartwater.billing.resident.entity.ApartmentOccupancy.OccupancyStatus;
import com.smartwater.billing.resident.entity.Resident;
import com.smartwater.billing.resident.entity.ResidentInvitation;
import com.smartwater.billing.common.entity.User;
import com.smartwater.billing.common.entity.WaterMeter;
import com.smartwater.billing.common.exception.BadRequestException;
import com.smartwater.billing.common.exception.ResourceNotFoundException;
import com.smartwater.billing.owner.repository.ApartmentRepository;
import com.smartwater.billing.owner.repository.BuildingOwnerRepository;
import com.smartwater.billing.owner.repository.BuildingRepository;
import com.smartwater.billing.resident.repository.ApartmentOccupancyRepository;
import com.smartwater.billing.resident.repository.ResidentInvitationRepository;
import com.smartwater.billing.resident.repository.ResidentRepository;
import com.smartwater.billing.common.repository.UserRepository;
import com.smartwater.billing.common.repository.WaterMeterRepository;
import com.smartwater.billing.resident.dto.OccupancyDTOs.*;
import com.smartwater.billing.resident.entity.OccupancyRequest;
import com.smartwater.billing.resident.entity.OccupancyRequest.RequestStatus;
import com.smartwater.billing.resident.repository.OccupancyRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApartmentService {

    @PersistenceContext
    private EntityManager entityManager;

    private final ApartmentRepository apartmentRepository;
    private final ApartmentOccupancyRepository occupancyRepository;
    private final OccupancyRequestRepository occupancyRequestRepository;
    private final BuildingRepository buildingRepository;
    private final BuildingOwnerRepository ownerRepository;
    private final ResidentRepository residentRepository;
    private final ResidentInvitationRepository invitationRepository;
    private final WaterMeterRepository waterMeterRepository;
    private final UserRepository userRepository;

    public ApartmentService(
            ApartmentRepository apartmentRepository,
            ApartmentOccupancyRepository occupancyRepository,
            OccupancyRequestRepository occupancyRequestRepository,
            BuildingRepository buildingRepository,
            BuildingOwnerRepository ownerRepository,
            ResidentRepository residentRepository,
            ResidentInvitationRepository invitationRepository,
            WaterMeterRepository waterMeterRepository,
            UserRepository userRepository
    ) {
        this.apartmentRepository = apartmentRepository;
        this.occupancyRepository = occupancyRepository;
        this.occupancyRequestRepository = occupancyRequestRepository;
        this.buildingRepository = buildingRepository;
        this.ownerRepository = ownerRepository;
        this.residentRepository = residentRepository;
        this.invitationRepository = invitationRepository;
        this.waterMeterRepository = waterMeterRepository;
        this.userRepository = userRepository;
    }

    private User validateUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private Building getBuildingAndVerifyOwner(String email, Long buildingId) {
        User user = validateUser(email);
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + buildingId));

        if (user.getRole() == User.Role.SUPER_ADMIN) {
            return building;
        }

        BuildingOwner owner = ownerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Owner profile not found"));

        if (!building.getOwner().getOwnerId().equals(owner.getOwnerId())) {
            throw new BadRequestException("Unauthorized access: You do not own building '" + building.getBuildingName() + "'!");
        }

        return building;
    }

    @Transactional
    public void ensureDefaultApartmentsForBuilding(Building building) {
        if (building == null) return;
        List<Apartment> existing = apartmentRepository.findByBuildingBuildingId(building.getBuildingId());
        if (!existing.isEmpty()) {
            return;
        }

        // Check if there are existing residents for this building that don't have an apartment created
        List<Resident> residents = residentRepository.findByBuildingBuildingId(building.getBuildingId());
        Set<String> createdRooms = new HashSet<>();

        for (Resident r : residents) {
            String room = r.getApartmentNumber() != null ? r.getApartmentNumber().trim() : "";
            if (!room.isEmpty() && !room.startsWith("FORMER-") && !room.equalsIgnoreCase("PENDING") && !createdRooms.contains(room)) {
                int floor = 1;
                try {
                    if (room.length() >= 3) {
                        floor = Integer.parseInt(room.substring(0, 1));
                    }
                } catch (Exception ignored) {}

                Apartment apt = Apartment.builder()
                        .building(building)
                        .floorNumber(floor)
                        .apartmentName("Flat " + room)
                        .roomNumber(room)
                        .unitType("2BHK")
                        .build();
                apt = apartmentRepository.save(apt);
                createdRooms.add(room);

                // If resident is active, establish active occupancy
                if (r.getAccountStatus() == Resident.AccountStatus.ACTIVE) {
                    Optional<ApartmentOccupancy> existingOcc = occupancyRepository.findFirstByApartmentApartmentIdAndStatus(apt.getApartmentId(), OccupancyStatus.ACTIVE);
                    if (existingOcc.isEmpty()) {
                        ApartmentOccupancy occ = ApartmentOccupancy.builder()
                                .apartment(apt)
                                .resident(r)
                                .startDate(r.getCreatedAt() != null ? r.getCreatedAt() : LocalDateTime.now())
                                .status(OccupancyStatus.ACTIVE)
                                .build();
                        occupancyRepository.save(occ);
                    }
                }

                // Link WaterMeter if present
                WaterMeter meter = waterMeterRepository.findByResidentResidentId(r.getResidentId()).orElse(null);
                if (meter != null && meter.getApartment() == null) {
                    meter.setApartment(apt);
                    waterMeterRepository.save(meter);
                }
            }
        }

        // If totalFlats specified and few created, generate default vacant flats
        int total = building.getTotalFlats() != null ? building.getTotalFlats() : 6;
        int flatsPerFloor = 4;
        for (int i = 1; createdRooms.size() < total && i <= 10; i++) {
            for (int f = 1; f <= flatsPerFloor && createdRooms.size() < total; f++) {
                String room = (i * 100 + f) + "";
                if (!createdRooms.contains(room)) {
                    Apartment apt = Apartment.builder()
                            .building(building)
                            .floorNumber(i)
                            .apartmentName("Flat " + room)
                            .roomNumber(room)
                            .unitType("2BHK")
                            .build();
                    apt = apartmentRepository.save(apt);
                    createdRooms.add(room);

                    String mNum = "WM-" + building.getBuildingNumber() + "-" + room;
                    if (waterMeterRepository.findByMeterNumber(mNum).isEmpty()) {
                        WaterMeter meter = WaterMeter.builder()
                                .building(building)
                                .apartment(apt)
                                .meterNumber(mNum)
                                .installationDate(LocalDate.now())
                                .status(WaterMeter.MeterStatus.ACTIVE)
                                .build();
                        waterMeterRepository.save(meter);
                    }
                }
            }
        }
    }

    @Transactional
    public BuildingInventoryResponse generateMissingUnitsForBuilding(String ownerEmail, Long buildingId) {
        Building building = getBuildingAndVerifyOwner(ownerEmail, buildingId);
        List<Apartment> existing = apartmentRepository.findByBuildingBuildingId(building.getBuildingId());
        Set<String> createdRooms = existing.stream().map(Apartment::getRoomNumber).collect(Collectors.toSet());

        int total = building.getTotalFlats() != null ? building.getTotalFlats() : 24;
        int flatsPerFloor = 4;
        for (int i = 1; createdRooms.size() < total && i <= 20; i++) {
            for (int f = 1; f <= flatsPerFloor && createdRooms.size() < total; f++) {
                String room = (i * 100 + f) + "";
                if (!createdRooms.contains(room)) {
                    Apartment apt = Apartment.builder()
                            .building(building)
                            .floorNumber(i)
                            .apartmentName("Flat " + room)
                            .roomNumber(room)
                            .unitType("2BHK")
                            .build();
                    apt = apartmentRepository.save(apt);
                    createdRooms.add(room);

                    String mNum = "WM-" + building.getBuildingNumber() + "-" + room;
                    if (waterMeterRepository.findByMeterNumber(mNum).isEmpty()) {
                        WaterMeter meter = WaterMeter.builder()
                                .building(building)
                                .apartment(apt)
                                .meterNumber(mNum)
                                .installationDate(LocalDate.now())
                                .status(WaterMeter.MeterStatus.ACTIVE)
                                .build();
                        waterMeterRepository.save(meter);
                    }
                }
            }
        }
        return getBuildingInventory(ownerEmail, buildingId);
    }

    @Transactional
    public void vacateApartment(String ownerEmail, Long apartmentId) {
        Apartment apt = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found with id: " + apartmentId));
        getBuildingAndVerifyOwner(ownerEmail, apt.getBuilding().getBuildingId());

        Optional<ApartmentOccupancy> activeOcc = occupancyRepository.findFirstByApartmentApartmentIdAndStatus(apartmentId, OccupancyStatus.ACTIVE);
        if (activeOcc.isPresent()) {
            ApartmentOccupancy occ = activeOcc.get();
            occ.setStatus(OccupancyStatus.ENDED);
            occ.setEndDate(LocalDateTime.now());
            occ.setMoveOutReason("Vacated by Building Owner");
            occupancyRepository.save(occ);

            Resident resident = occ.getResident();
            if (resident != null) {
                resident.setApartmentNumber("FORMER-" + apt.getRoomNumber());
                resident.setAccountStatus(Resident.AccountStatus.INACTIVE);
                residentRepository.save(resident);
            }
        }

        // Also reject/cancel any pending requests for this apartment
        List<OccupancyRequest> reqs = occupancyRequestRepository.findAll().stream()
                .filter(r -> r.getApartment() != null && r.getApartment().getApartmentId().equals(apartmentId) && r.getStatus() == OccupancyRequest.RequestStatus.PENDING)
                .collect(Collectors.toList());
        for (OccupancyRequest r : reqs) {
            r.setStatus(OccupancyRequest.RequestStatus.REJECTED);
            r.setRejectionReason("Unit vacated or occupancy terminated by owner");
            r.setReviewedAt(LocalDateTime.now());
            occupancyRequestRepository.save(r);
        }

        // Unlink meter resident
        List<WaterMeter> meters = waterMeterRepository.findByBuildingBuildingId(apt.getBuilding().getBuildingId());
        for (WaterMeter m : meters) {
            if (m.getApartment() != null && m.getApartment().getApartmentId().equals(apartmentId)) {
                m.setResident(null);
                waterMeterRepository.save(m);
            }
        }
    }

    @Transactional
    public void cancelInvitation(String ownerEmail, Long invitationId) {
        ResidentInvitation inv = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found: " + invitationId));
        getBuildingAndVerifyOwner(ownerEmail, inv.getBuilding().getBuildingId());
        inv.setStatus(ResidentInvitation.InvitationStatus.CANCELLED);
        invitationRepository.save(inv);
    }

    @Transactional
    public BuildingInventoryResponse getBuildingInventory(String ownerEmail, Long buildingId) {
        Building building = getBuildingAndVerifyOwner(ownerEmail, buildingId);
        ensureDefaultApartmentsForBuilding(building);

        List<Apartment> apartments = apartmentRepository.findByBuildingBuildingIdOrderByFloorNumberAscRoomNumberAsc(buildingId);

        List<ApartmentResponse> responseList = apartments.stream()
                .map(this::enrichApartmentResponse)
                .collect(Collectors.toList());

        // Group by floor
        Map<Integer, List<ApartmentResponse>> groupedByFloor = responseList.stream()
                .collect(Collectors.groupingBy(ApartmentResponse::getFloorNumber, TreeMap::new, Collectors.toList()));

        List<FloorGroupResponse> floors = groupedByFloor.entrySet().stream()
                .map(e -> new FloorGroupResponse(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        int occupied = (int) responseList.stream().filter(a -> "OCCUPIED".equals(a.getOccupancyStatus())).count();
        int pending = (int) responseList.stream().filter(a -> a.getOccupancyStatus() != null && a.getOccupancyStatus().startsWith("PENDING")).count();
        int vacant = (int) responseList.stream().filter(a -> "VACANT".equals(a.getOccupancyStatus())).count();

        BuildingInventoryResponse inventory = new BuildingInventoryResponse();
        inventory.setBuildingId(building.getBuildingId());
        inventory.setBuildingName(building.getBuildingName());
        inventory.setBuildingNumber(building.getBuildingNumber());
        inventory.setLocation(building.getLocation());
        inventory.setTotalFlats(building.getTotalFlats());
        inventory.setConfiguredUnitsCount(responseList.size());
        inventory.setOccupiedCount(occupied);
        inventory.setVacantCount(vacant);
        inventory.setPendingCount(pending);
        inventory.setFloors(floors);

        return inventory;
    }

    @Transactional
    public ApartmentResponse createApartment(String ownerEmail, Long buildingId, CreateApartmentRequest request) {
        Building building = getBuildingAndVerifyOwner(ownerEmail, buildingId);

        String room = request.getRoomNumber().trim();
        if (apartmentRepository.existsByBuildingBuildingIdAndRoomNumber(buildingId, room)) {
            throw new BadRequestException("Apartment or Room number '" + room + "' already exists in building '" + building.getBuildingName() + "'!");
        }

        Apartment apartment = Apartment.builder()
                .building(building)
                .floorNumber(request.getFloorNumber() != null ? request.getFloorNumber() : 1)
                .apartmentName(request.getApartmentName().trim())
                .roomNumber(room)
                .unitType(request.getUnitType() != null ? request.getUnitType().trim() : "2BHK")
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .build();

        apartment = apartmentRepository.save(apartment);

        // Ensure physical water meter fixture exists for this unit
        String mNum = "WM-" + building.getBuildingNumber() + "-" + room;
        WaterMeter meter = waterMeterRepository.findByMeterNumber(mNum).orElse(null);
        if (meter == null) {
            meter = WaterMeter.builder()
                    .building(building)
                    .apartment(apartment)
                    .meterNumber(mNum)
                    .installationDate(LocalDate.now())
                    .status(WaterMeter.MeterStatus.ACTIVE)
                    .build();
            waterMeterRepository.save(meter);
        } else if (meter.getApartment() == null) {
            meter.setApartment(apartment);
            waterMeterRepository.save(meter);
        }

        return enrichApartmentResponse(apartment);
    }

    @Transactional
    public List<ApartmentResponse> batchCreateApartments(String ownerEmail, BatchCreateApartmentsRequest request) {
        Building building = getBuildingAndVerifyOwner(ownerEmail, request.getBuildingId());

        List<ApartmentResponse> result = new ArrayList<>();
        for (String room : request.getRoomNumbers()) {
            String trimmedRoom = room.trim();
            if (trimmedRoom.isEmpty() || apartmentRepository.existsByBuildingBuildingIdAndRoomNumber(building.getBuildingId(), trimmedRoom)) {
                continue;
            }

            Apartment apartment = Apartment.builder()
                    .building(building)
                    .floorNumber(request.getFloorNumber())
                    .apartmentName("Flat " + trimmedRoom)
                    .roomNumber(trimmedRoom)
                    .unitType(request.getUnitType() != null ? request.getUnitType() : "2BHK")
                    .build();

            apartment = apartmentRepository.save(apartment);

            // Ensure meter fixture exists
            String mNum = "WM-" + building.getBuildingNumber() + "-" + trimmedRoom;
            WaterMeter meter = waterMeterRepository.findByMeterNumber(mNum).orElse(null);
            if (meter == null) {
                meter = WaterMeter.builder()
                        .building(building)
                        .apartment(apartment)
                        .meterNumber(mNum)
                        .installationDate(LocalDate.now())
                        .status(WaterMeter.MeterStatus.ACTIVE)
                        .build();
                waterMeterRepository.save(meter);
            } else if (meter.getApartment() == null) {
                meter.setApartment(apartment);
                waterMeterRepository.save(meter);
            }

            result.add(enrichApartmentResponse(apartment));
        }

        return result;
    }

    @Transactional
    public void deleteApartment(String ownerEmail, Long apartmentId) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found with id: " + apartmentId));

        getBuildingAndVerifyOwner(ownerEmail, apartment.getBuilding().getBuildingId());

        Long bldgId = apartment.getBuilding().getBuildingId();
        String room = apartment.getRoomNumber();
        String mNum = "WM-" + apartment.getBuilding().getBuildingNumber() + "-" + room;

        // 1. Delete occupancy requests for this apartment
        entityManager.createNativeQuery("DELETE FROM occupancy_requests WHERE apartment_id = :aptId")
                .setParameter("aptId", apartmentId)
                .executeUpdate();

        // 2. Delete invitations for this apartment
        entityManager.createNativeQuery("DELETE FROM resident_invitations WHERE apartment_id = :aptId")
                .setParameter("aptId", apartmentId)
                .executeUpdate();

        // 3. Delete occupancies for this apartment
        entityManager.createNativeQuery("DELETE FROM apartment_occupancies WHERE apartment_id = :aptId")
                .setParameter("aptId", apartmentId)
                .executeUpdate();

        // 4. Delete water usage for meter associated with this apartment or meter number
        entityManager.createNativeQuery(
                "DELETE FROM water_usage WHERE meter_id IN (SELECT m.meter_id FROM (SELECT meter_id FROM water_meters WHERE apartment_id = :aptId OR meter_number = :mNum) m)"
        ).setParameter("aptId", apartmentId).setParameter("mNum", mNum).executeUpdate();

        // 5. Delete water meters assigned to this apartment or meter number
        entityManager.createNativeQuery("DELETE FROM water_meters WHERE apartment_id = :aptId OR meter_number = :mNum")
                .setParameter("aptId", apartmentId)
                .setParameter("mNum", mNum)
                .executeUpdate();

        // 6. Gracefully update any resident referencing this apartment number in this building
        entityManager.createNativeQuery(
                "UPDATE residents SET apartment_number = CONCAT('FORMER-', apartment_number), account_status = 'INACTIVE' " +
                "WHERE building_id = :bldgId AND apartment_number = :roomNum"
        ).setParameter("bldgId", bldgId).setParameter("roomNum", room).executeUpdate();

        // 7. Delete apartment record
        entityManager.createNativeQuery("DELETE FROM apartments WHERE apartment_id = :aptId")
                .setParameter("aptId", apartmentId)
                .executeUpdate();
    }

    @Transactional(readOnly = true)
    public List<ApartmentResponse> getAllOwnerApartments(String ownerEmail) {
        User user = validateUser(ownerEmail);
        List<Apartment> apartments;
        if (user.getRole() == User.Role.SUPER_ADMIN) {
            apartments = apartmentRepository.findAll();
        } else {
            BuildingOwner owner = ownerRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Owner profile not found"));
            apartments = apartmentRepository.findByBuildingOwnerOwnerIdOrderByBuildingBuildingNameAscFloorNumberAscRoomNumberAsc(owner.getOwnerId());
        }

        return apartments.stream()
                .map(this::enrichApartmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PublicOwnerResponse> getPublicOwners() {
        List<BuildingOwner> owners = ownerRepository.findAll();
        return owners.stream()
                .map(owner -> {
                    String name = owner.getUser() != null && owner.getUser().getName() != null
                            ? owner.getUser().getName()
                            : (owner.getName() != null ? owner.getName() : "Owner #" + owner.getOwnerId());
                    int buildingCount = buildingRepository.findByOwnerOwnerId(owner.getOwnerId()).size();
                    return new PublicOwnerResponse(
                            owner.getOwnerId(),
                            name,
                            owner.getPhone() != null ? owner.getPhone() : "",
                            buildingCount
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PublicBuildingResponse> getOwnerBuildings(Long ownerId) {
        List<Building> buildings = buildingRepository.findByOwnerOwnerId(ownerId);
        return buildings.stream()
                .map(b -> {
                    int vacantCount = (int) getAvailableApartments(b.getBuildingId(), null).size();
                    return new PublicBuildingResponse(
                            b.getBuildingId(),
                            b.getBuildingName(),
                            b.getBuildingNumber(),
                            b.getLocation(),
                            b.getTotalFlats(),
                            vacantCount
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AvailableApartmentResponse> getAvailableApartments(Long buildingId, Integer floorNumber) {
        List<Apartment> apartments;
        if (buildingId != null && floorNumber != null) {
            apartments = apartmentRepository.findByBuildingAndFloor(buildingId, floorNumber);
        } else if (buildingId != null) {
            apartments = apartmentRepository.findByBuildingBuildingIdOrderByFloorNumberAscRoomNumberAsc(buildingId);
        } else {
            apartments = apartmentRepository.findAll();
        }

        List<AvailableApartmentResponse> available = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Apartment apt : apartments) {
            // Check active occupancy
            Optional<ApartmentOccupancy> activeOcc = occupancyRepository.findFirstByApartmentApartmentIdAndStatus(apt.getApartmentId(), OccupancyStatus.ACTIVE);
            if (activeOcc.isPresent()) {
                continue; // Occupied
            }

            // Check if there is an active PENDING occupancy request for this unit
            Optional<OccupancyRequest> pendingReq = occupancyRequestRepository.findActiveRequestByApartmentId(apt.getApartmentId(), RequestStatus.PENDING);
            if (pendingReq.isPresent()) {
                continue; // Reserved under pending occupancy request
            }

            // Check pending invitations for this unit/room in the building
            List<ResidentInvitation> pendingInvites = invitationRepository.findByBuildingBuildingIdAndStatus(
                    apt.getBuilding().getBuildingId(),
                    ResidentInvitation.InvitationStatus.PENDING
            );
            boolean hasPendingInvite = pendingInvites.stream().anyMatch(inv ->
                    (inv.getApartment() != null && inv.getApartment().getApartmentId().equals(apt.getApartmentId()))
                            || (inv.getApartmentNumber() != null && inv.getApartmentNumber().equalsIgnoreCase(apt.getRoomNumber()))
                            && inv.getExpiresAt() != null && inv.getExpiresAt().isAfter(now)
            );

            if (hasPendingInvite) {
                continue; // Reserved under pending invitation
            }

            // Get meter number fixture
            WaterMeter meter = waterMeterRepository.findByBuildingBuildingId(apt.getBuilding().getBuildingId()).stream()
                    .filter(m -> (m.getApartment() != null && m.getApartment().getApartmentId().equals(apt.getApartmentId()))
                            || (m.getMeterNumber() != null && m.getMeterNumber().endsWith("-" + apt.getRoomNumber())))
                    .findFirst()
                    .orElse(null);

            available.add(AvailableApartmentResponse.builder()
                    .apartmentId(apt.getApartmentId())
                    .buildingId(apt.getBuilding().getBuildingId())
                    .buildingName(apt.getBuilding().getBuildingName())
                    .buildingNumber(apt.getBuilding().getBuildingNumber())
                    .location(apt.getBuilding().getLocation())
                    .floorNumber(apt.getFloorNumber())
                    .apartmentName(apt.getApartmentName())
                    .roomNumber(apt.getRoomNumber())
                    .unitType(apt.getUnitType())
                    .description(apt.getDescription())
                    .meterNumber(meter != null ? meter.getMeterNumber() : "N/A")
                    .build());
        }

        return available;
    }

    public ApartmentResponse enrichApartmentResponse(Apartment apt) {
        ApartmentResponse.Builder builder = ApartmentResponse.builder()
                .apartmentId(apt.getApartmentId())
                .buildingId(apt.getBuilding().getBuildingId())
                .buildingName(apt.getBuilding().getBuildingName())
                .buildingNumber(apt.getBuilding().getBuildingNumber())
                .location(apt.getBuilding().getLocation())
                .floorNumber(apt.getFloorNumber())
                .apartmentName(apt.getApartmentName())
                .roomNumber(apt.getRoomNumber())
                .unitType(apt.getUnitType())
                .description(apt.getDescription())
                .createdAt(apt.getCreatedAt());

        // Check active occupancy
        Optional<ApartmentOccupancy> activeOcc = occupancyRepository.findFirstByApartmentApartmentIdAndStatus(apt.getApartmentId(), OccupancyStatus.ACTIVE);
        if (activeOcc.isPresent()) {
            ApartmentOccupancy occ = activeOcc.get();
            Resident res = occ.getResident();
            builder.occupancyStatus("OCCUPIED")
                    .currentResidentId(res.getResidentId())
                    .currentResidentName(res.getName())
                    .currentResidentEmail(res.getEmail())
                    .currentResidentPhone(res.getPhone())
                    .activeOccupancyId(occ.getOccupancyId())
                    .occupiedSince(occ.getStartDate());
        } else {
            // Check active PENDING occupancy request
            Optional<OccupancyRequest> pendingReq = occupancyRequestRepository.findActiveRequestByApartmentId(apt.getApartmentId(), RequestStatus.PENDING);
            if (pendingReq.isPresent()) {
                OccupancyRequest req = pendingReq.get();
                Resident res = req.getResident();
                builder.occupancyStatus("PENDING_APPROVAL")
                        .pendingRequestId(req.getRequestId())
                        .currentResidentId(res.getResidentId())
                        .currentResidentName(res.getName())
                        .currentResidentEmail(res.getEmail())
                        .currentResidentPhone(res.getPhone());
            } else {
                // Check pending invitation
                LocalDateTime now = LocalDateTime.now();
                List<ResidentInvitation> pendingInvites = invitationRepository.findByBuildingBuildingIdAndStatus(
                        apt.getBuilding().getBuildingId(),
                        ResidentInvitation.InvitationStatus.PENDING
                );
                ResidentInvitation matchingInvite = pendingInvites.stream().filter(inv ->
                        ((inv.getApartment() != null && inv.getApartment().getApartmentId().equals(apt.getApartmentId()))
                                || (inv.getApartmentNumber() != null && inv.getApartmentNumber().equalsIgnoreCase(apt.getRoomNumber())))
                                && inv.getExpiresAt() != null && inv.getExpiresAt().isAfter(now)
                ).findFirst().orElse(null);

                if (matchingInvite != null) {
                    builder.occupancyStatus("PENDING_INVITATION")
                            .pendingInvitationId(matchingInvite.getId())
                            .currentResidentName(matchingInvite.getResidentName())
                            .currentResidentEmail(matchingInvite.getResidentEmail())
                            .currentResidentPhone(matchingInvite.getPhoneNumber());
                } else {
                    builder.occupancyStatus("VACANT");
                }
            }
        }

        // Find linked water meter
        WaterMeter meter = waterMeterRepository.findByBuildingBuildingId(apt.getBuilding().getBuildingId()).stream()
                .filter(m -> (m.getApartment() != null && m.getApartment().getApartmentId().equals(apt.getApartmentId()))
                        || (m.getMeterNumber() != null && m.getMeterNumber().endsWith("-" + apt.getRoomNumber())))
                .findFirst()
                .orElse(null);

        if (meter != null) {
            builder.meterNumber(meter.getMeterNumber());
        }

        return builder.build();
    }
}
