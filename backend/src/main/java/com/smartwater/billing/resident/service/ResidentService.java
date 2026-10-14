package com.smartwater.billing.resident.service;

import com.smartwater.billing.common.dto.AuthDTOs.MessageResponse;
import com.smartwater.billing.common.dto.AuthDTOs.ValidateInvitationResponse;
import com.smartwater.billing.common.dto.DashboardDTOs.*;
import com.smartwater.billing.resident.dto.ResidentDTOs.*;
import com.smartwater.billing.common.entity.*;
import com.smartwater.billing.owner.entity.*;
import com.smartwater.billing.resident.entity.*;
import com.smartwater.billing.common.exception.BadRequestException;
import com.smartwater.billing.common.exception.ResourceNotFoundException;
import com.smartwater.billing.owner.service.BuildingOwnerService;
import com.smartwater.billing.common.repository.*;
import com.smartwater.billing.owner.repository.*;
import com.smartwater.billing.resident.repository.*;
import com.smartwater.billing.common.service.EmailService;
import com.smartwater.billing.common.util.TokenGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ResidentService {

    private final ResidentRepository residentRepository;
    private final ResidentInvitationRepository invitationRepository;
    private final BuildingRepository buildingRepository;
    private final WaterMeterRepository meterRepository;
    private final BillRepository billRepository;
    private final WaterUsageRepository usageRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final BuildingOwnerService ownerService;
    private final EmailService emailService;
    private final ApartmentRepository apartmentRepository;
    private final ApartmentOccupancyRepository occupancyRepository;
    private final OccupancyRequestRepository occupancyRequestRepository;

    public ResidentService(
            ResidentRepository residentRepository,
            ResidentInvitationRepository invitationRepository,
            BuildingRepository buildingRepository,
            WaterMeterRepository meterRepository,
            BillRepository billRepository,
            WaterUsageRepository usageRepository,
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            BuildingOwnerService ownerService,
            EmailService emailService,
            ApartmentRepository apartmentRepository,
            ApartmentOccupancyRepository occupancyRepository,
            OccupancyRequestRepository occupancyRequestRepository
    ) {
        this.residentRepository = residentRepository;
        this.invitationRepository = invitationRepository;
        this.buildingRepository = buildingRepository;
        this.meterRepository = meterRepository;
        this.billRepository = billRepository;
        this.usageRepository = usageRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.ownerService = ownerService;
        this.emailService = emailService;
        this.apartmentRepository = apartmentRepository;
        this.occupancyRepository = occupancyRepository;
        this.occupancyRequestRepository = occupancyRequestRepository;
    }

    @Value("${resident.invitation.expiration-hours:24}")
    private int expirationHours;

    @Transactional
    public void ensureDefaultResidentsForOwner(BuildingOwner owner) {
        // No-op: resident records are created exclusively through user self-registration
    }

    @Transactional
    public ResidentResponse inviteResident(String ownerEmail, InviteResidentRequest request) {
        BuildingOwner owner = ownerService.getOwnerByUserEmail(ownerEmail);
        Building building = buildingRepository.findById(request.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + request.getBuildingId()));

        if (!building.getOwner().getOwnerId().equals(owner.getOwnerId())) {
            throw new BadRequestException("Unauthorized access: You do not own building '" + building.getBuildingName() + "'!");
        }

        // Check if an ACTIVE user already exists for this email
        Optional<User> existingUserOpt = userRepository.findByEmail(request.getEmail().trim().toLowerCase());
        if (existingUserOpt.isPresent() && existingUserOpt.get().getStatus() == User.UserStatus.ACTIVE) {
            throw new BadRequestException("An active account already exists for " + request.getEmail() + ". Invitation rejected.");
        }

        // Resolve or create Apartment
        String room = request.getApartmentNumber() != null ? request.getApartmentNumber().trim() : "";
        Apartment apt = null;
        if (request.getApartmentId() != null) {
            apt = apartmentRepository.findById(request.getApartmentId()).orElse(null);
            if (apt != null) {
                room = apt.getRoomNumber();
            }
        }
        if (apt == null && !room.isEmpty()) {
            apt = apartmentRepository.findByBuildingBuildingIdAndRoomNumber(building.getBuildingId(), room).orElse(null);
            if (apt == null) {
                int floor = 1;
                try {
                    if (room.length() >= 3) {
                        floor = Integer.parseInt(room.substring(0, 1));
                    }
                } catch (Exception ignored) {}
                apt = Apartment.builder()
                        .building(building)
                        .floorNumber(floor)
                        .apartmentName("Flat " + room)
                        .roomNumber(room)
                        .unitType("2BHK")
                        .build();
                apt = apartmentRepository.save(apt);
            }
        }

        // Validate that apartment is NOT already occupied
        if (apt != null) {
            Optional<ApartmentOccupancy> activeOcc = occupancyRepository.findFirstByApartmentApartmentIdAndStatus(apt.getApartmentId(), ApartmentOccupancy.OccupancyStatus.ACTIVE);
            if (activeOcc.isPresent()) {
                throw new BadRequestException("Apartment " + apt.getApartmentName() + " is already occupied by " + activeOcc.get().getResident().getName() + ". You cannot invite another resident to an occupied apartment.");
            }
        }

        // Check if resident profile exists
        Resident resident = residentRepository.findByEmailIgnoreCase(request.getEmail().trim())
                .orElse(null);

        String rawToken = TokenGenerator.generateRawToken();
        String tokenHash = TokenGenerator.hashToken(rawToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(expirationHours);

        if (resident == null) {
            resident = Resident.builder()
                    .building(building)
                    .name(request.getName().trim())
                    .email(request.getEmail().trim().toLowerCase())
                    .phone(request.getPhone() != null ? request.getPhone().trim() : "")
                    .apartmentNumber(room)
                    .invitationToken(rawToken)
                    .tokenHash(tokenHash)
                    .expiresAt(expiresAt)
                    .invitationStatus(Resident.InvitationStatus.PENDING)
                    .accountStatus(Resident.AccountStatus.INACTIVE)
                    .build();
        } else {
            resident.setBuilding(building);
            resident.setName(request.getName().trim());
            if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
                resident.setPhone(request.getPhone().trim());
            }
            resident.setApartmentNumber(room);
            resident.setInvitationToken(rawToken);
            resident.setTokenHash(tokenHash);
            resident.setExpiresAt(expiresAt);
            resident.setInvitationStatus(Resident.InvitationStatus.PENDING);
            resident.setAccountStatus(Resident.AccountStatus.INACTIVE);
        }

        resident = residentRepository.save(resident);

        // Cancel previous pending invitations for this resident
        List<ResidentInvitation> prevList = invitationRepository.findByResidentEmailOrderByCreatedAtDesc(resident.getEmail());
        for (ResidentInvitation prev : prevList) {
            if (prev.getStatus() == ResidentInvitation.InvitationStatus.PENDING) {
                prev.setStatus(ResidentInvitation.InvitationStatus.CANCELLED);
                invitationRepository.save(prev);
            }
        }

        ResidentInvitation invitation = ResidentInvitation.builder()
                .resident(resident)
                .building(building)
                .apartment(apt)
                .invitedBy(owner)
                .residentName(resident.getName())
                .residentEmail(resident.getEmail())
                .phoneNumber(resident.getPhone())
                .apartmentNumber(room)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .status(ResidentInvitation.InvitationStatus.PENDING)
                .build();
        invitationRepository.save(invitation);

        // Assign or reuse Water Meter
        WaterMeter meter = meterRepository.findFirstByResidentResidentId(resident.getResidentId()).orElse(null);
        if (meter == null) {
            String mNum = "WM-" + building.getBuildingNumber() + "-" + room;
            meter = meterRepository.findByMeterNumber(mNum).orElse(null);
            if (meter == null) {
                meter = WaterMeter.builder()
                        .building(building)
                        .apartment(apt)
                        .resident(resident)
                        .meterNumber(mNum)
                        .installationDate(LocalDate.now())
                        .status(WaterMeter.MeterStatus.ACTIVE)
                        .build();
            } else {
                meter.setBuilding(building);
                meter.setApartment(apt);
                meter.setResident(resident);
                meter.setStatus(WaterMeter.MeterStatus.ACTIVE);
            }
            meterRepository.save(meter);
        } else if (apt != null && meter.getApartment() == null) {
            meter.setApartment(apt);
            meterRepository.save(meter);
        }

        // Send Email Notification
        String ownerName = owner.getUser() != null ? owner.getUser().getName() : owner.getName();
        boolean emailSent = emailService.sendResidentInvitation(resident.getEmail(), resident.getName(), ownerName, building.getBuildingName(), room, rawToken);

        ResidentResponse response = convertToResidentResponse(resident);
        response.setEmailSent(emailSent);
        return response;
    }

    public ValidateInvitationResponse validateInvitationToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new BadRequestException("Invitation token parameter is required.");
        }

        String tokenHash = TokenGenerator.hashToken(token.trim());
        ResidentInvitation invitation = invitationRepository.findByTokenHash(tokenHash)
                .or(() -> invitationRepository.findByTokenHash(token.trim()))
                .orElse(null);

        Resident resident = null;
        if (invitation != null) {
            resident = invitation.getResident();
        } else {
            resident = residentRepository.findByInvitationToken(token.trim())
                    .or(() -> residentRepository.findByTokenHash(tokenHash))
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired invitation link. Please request a new invitation from your building owner."));
        }

        if (resident.getInvitationStatus() == Resident.InvitationStatus.ACCEPTED && resident.getUser() != null) {
            throw new BadRequestException("This invitation link has already been accepted. Please proceed to login.");
        }

        if (resident.getExpiresAt() != null && resident.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("This invitation link has expired (valid for 24 hours). Please contact your building owner to resend the invitation.");
        }

        String name = resident.getName();
        String email = resident.getEmail();
        String buildingName = resident.getBuilding() != null ? resident.getBuilding().getBuildingName() : "N/A";
        String apartmentNumber = resident.getApartmentNumber();
        LocalDateTime expiresAt = resident.getExpiresAt();

        return new ValidateInvitationResponse("PENDING", name, email, buildingName, apartmentNumber, expiresAt != null ? expiresAt.toString() : null, "Valid invitation token.");
    }

    public ValidateInvitationResponse validateInvitation(String token) {
        return validateInvitationToken(token);
    }

    @Transactional
    public MessageResponse resendInvitation(String ownerEmail, Long residentId) {
        BuildingOwner owner = ownerService.getOwnerByUserEmail(ownerEmail);
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found with id: " + residentId));

        if (!resident.getBuilding().getOwner().getOwnerId().equals(owner.getOwnerId())) {
            throw new BadRequestException("Unauthorized access: You do not own the building for this resident!");
        }

        if (resident.getInvitationStatus() == Resident.InvitationStatus.ACCEPTED && resident.getUser() != null) {
            throw new BadRequestException("Resident account is already activated!");
        }

        // Cancel previous invitations for this resident
        List<ResidentInvitation> prevList = invitationRepository.findByResidentEmailOrderByCreatedAtDesc(resident.getEmail());
        for (ResidentInvitation prev : prevList) {
            if (prev.getStatus() == ResidentInvitation.InvitationStatus.PENDING) {
                prev.setStatus(ResidentInvitation.InvitationStatus.CANCELLED);
                invitationRepository.save(prev);
            }
        }

        // Generate new secure raw token and hash
        String newRawToken = TokenGenerator.generateRawToken();
        String newTokenHash = TokenGenerator.hashToken(newRawToken);
        LocalDateTime newExpiresAt = LocalDateTime.now().plusHours(expirationHours);

        resident.setInvitationToken(newRawToken);
        resident.setTokenHash(newTokenHash);
        resident.setExpiresAt(newExpiresAt);
        resident.setInvitationStatus(Resident.InvitationStatus.PENDING);
        resident.setAccountStatus(Resident.AccountStatus.INACTIVE);
        residentRepository.save(resident);

        ResidentInvitation newInvite = ResidentInvitation.builder()
                .resident(resident)
                .building(resident.getBuilding())
                .invitedBy(owner)
                .residentName(resident.getName())
                .residentEmail(resident.getEmail())
                .phoneNumber(resident.getPhone())
                .apartmentNumber(resident.getApartmentNumber())
                .tokenHash(newTokenHash)
                .expiresAt(newExpiresAt)
                .status(ResidentInvitation.InvitationStatus.PENDING)
                .build();
        invitationRepository.save(newInvite);

        // Dispatch new invitation email with newRawToken
        String ownerName = owner != null && owner.getUser() != null ? owner.getUser().getName() : "Building Owner";
        boolean emailSent = emailService.sendResidentInvitation(resident.getEmail(), resident.getName(), ownerName, resident.getBuilding().getBuildingName(), resident.getApartmentNumber(), newRawToken);

        if (emailSent) {
            return new MessageResponse("An account activation link has been sent to " + resident.getEmail(), true);
        } else {
            return new MessageResponse("Invitation created, but email delivery failed for " + resident.getEmail() + ". Please check SMTP email settings or try resending.", false);
        }
    }

    @Transactional
    public List<ResidentResponse> getOwnerResidents(String ownerEmail, String searchQuery, String statusFilter) {
        BuildingOwner owner = ownerService.getOwnerByUserEmail(ownerEmail);
        
        // Ensure default initial residents exist for this owner if they have buildings but no residents
        ensureDefaultResidentsForOwner(owner);

        String cleanQuery = (searchQuery != null && !searchQuery.trim().isEmpty()) ? searchQuery.trim() : null;
        Resident.AccountStatus status = null;
        if (statusFilter != null && !statusFilter.isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            try {
                status = Resident.AccountStatus.valueOf(statusFilter.toUpperCase());
            } catch (Exception ignored) {}
        }

        List<Resident> residents = residentRepository.searchResidentsForOwner(owner.getOwnerId(), cleanQuery, status);
        if (residents.isEmpty() && cleanQuery == null && status == null) {
            residents = residentRepository.findByBuildingOwnerOwnerId(owner.getOwnerId());
        }

        return residents.stream()
                .map(this::convertToResidentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ResidentResponse updateResident(String ownerEmail, Long residentId, UpdateResidentRequest request) {
        BuildingOwner owner = ownerService.getOwnerByUserEmail(ownerEmail);
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found with id: " + residentId));

        if (!resident.getBuilding().getOwner().getOwnerId().equals(owner.getOwnerId())) {
            throw new BadRequestException("Unauthorized access to resident!");
        }

        resident.setName(request.getName());
        resident.setPhone(request.getPhone());
        resident.setApartmentNumber(request.getApartmentNumber());
        residentRepository.save(resident);

        return convertToResidentResponse(resident);
    }

    @Transactional
    public void deleteResident(String ownerEmail, Long residentId) {
        BuildingOwner owner = ownerService.getOwnerByUserEmail(ownerEmail);
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found with id: " + residentId));

        if (!resident.getBuilding().getOwner().getOwnerId().equals(owner.getOwnerId())) {
            throw new BadRequestException("Unauthorized access to resident!");
        }

        // End active occupancy if any
        Optional<ApartmentOccupancy> activeOcc = occupancyRepository.findFirstByResidentResidentIdAndStatus(residentId, ApartmentOccupancy.OccupancyStatus.ACTIVE);
        if (activeOcc.isPresent()) {
            ApartmentOccupancy occ = activeOcc.get();
            occ.setStatus(ApartmentOccupancy.OccupancyStatus.ENDED);
            occ.setEndDate(LocalDateTime.now());
            occ.setMoveOutReason("Resident removed by building owner");
            occupancyRepository.save(occ);
        }

        // Detach water meter
        WaterMeter meter = meterRepository.findByResidentResidentId(residentId).orElse(null);
        if (meter != null) {
            meter.setResident(null);
            meterRepository.save(meter);
        }

        // Clean up requests submitted by this resident
        List<OccupancyRequest> reqs = occupancyRequestRepository.findAllByResidentId(residentId);
        if (!reqs.isEmpty()) {
            occupancyRequestRepository.deleteAll(reqs);
        }

        // Check if resident has billing history
        boolean hasBills = !billRepository.findByResidentResidentIdOrderByGeneratedAtDesc(residentId).isEmpty();
        if (hasBills) {
            resident.setAccountStatus(Resident.AccountStatus.INACTIVE);
            resident.setApartmentNumber("Vacated");
            residentRepository.save(resident);
        } else {
            List<ApartmentOccupancy> allOccs = occupancyRepository.findByResidentResidentIdOrderByStartDateDesc(residentId);
            if (!allOccs.isEmpty()) {
                occupancyRepository.deleteAll(allOccs);
            }
            residentRepository.delete(resident);
        }
    }

    @Transactional
    public void vacateResident(String ownerEmail, Long residentId) {
        BuildingOwner owner = ownerService.getOwnerByUserEmail(ownerEmail);
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found with id: " + residentId));

        if (!resident.getBuilding().getOwner().getOwnerId().equals(owner.getOwnerId())) {
            throw new BadRequestException("Unauthorized access to resident!");
        }

        // End active occupancy
        Optional<ApartmentOccupancy> activeOcc = occupancyRepository.findFirstByResidentResidentIdAndStatus(residentId, ApartmentOccupancy.OccupancyStatus.ACTIVE);
        if (activeOcc.isPresent()) {
            ApartmentOccupancy occ = activeOcc.get();
            occ.setStatus(ApartmentOccupancy.OccupancyStatus.ENDED);
            occ.setEndDate(LocalDateTime.now());
            occ.setMoveOutReason("Vacated by building owner");
            occupancyRepository.save(occ);
        }

        // Detach water meter
        WaterMeter meter = meterRepository.findByResidentResidentId(residentId).orElse(null);
        if (meter != null) {
            meter.setResident(null);
            meterRepository.save(meter);
        }

        resident.setApartmentNumber("Vacated");
        resident.setAccountStatus(Resident.AccountStatus.INACTIVE);
        residentRepository.save(resident);
    }

    public ResidentDashboardStats getResidentDashboardStats(String residentEmail) {
        Resident resident = residentRepository.findByUserEmail(residentEmail)
                .or(() -> residentRepository.findByEmail(residentEmail))
                .orElse(null);

        if (resident == null) {
            User user = userRepository.findByEmail(residentEmail).orElse(null);
            if (user != null && user.getRole() == User.Role.RESIDENT) {
                return ResidentDashboardStats.builder()
                        .residentName(user.getName())
                        .buildingName("No Active Apartment")
                        .apartmentNumber("Vacant / Not Associated")
                        .meterNumber("N/A")
                        .currentMonthUsage(0.0)
                        .activeBillAmount(0.0)
                        .activeBillStatus("NO_BILLS")
                        .activeBillDueDate("N/A")
                        .unreadNotificationsCount(0)
                        .currentBillingCycle("N/A")
                        .cycleStartDate("N/A")
                        .cycleEndDate("N/A")
                        .hasActiveApartment(false)
                        .build();
            }
            throw new ResourceNotFoundException("Resident profile not found for " + residentEmail);
        }

        Optional<ApartmentOccupancy> activeOcc = occupancyRepository.findActiveByResidentEmail(residentEmail, ApartmentOccupancy.OccupancyStatus.ACTIVE);
        boolean hasActive = activeOcc.isPresent();

        String buildingName = "No Active Apartment";
        String apartmentNumber = "Vacant / Not Associated";
        String meterNumber = "N/A";
        Double currentUsage = 0.0;

        if (hasActive) {
            ApartmentOccupancy occ = activeOcc.get();
            buildingName = occ.getApartment().getBuilding().getBuildingName();
            apartmentNumber = occ.getApartment().getRoomNumber();
            WaterMeter meter = meterRepository.findByResidentResidentId(resident.getResidentId()).orElse(null);
            if (meter == null) {
                meter = meterRepository.findByBuildingBuildingId(occ.getApartment().getBuilding().getBuildingId()).stream()
                        .filter(m -> (m.getApartment() != null && m.getApartment().getApartmentId().equals(occ.getApartment().getApartmentId()))
                                || (m.getMeterNumber() != null && m.getMeterNumber().endsWith("-" + occ.getApartment().getRoomNumber())))
                        .findFirst()
                        .orElse(null);
            }
            if (meter != null) {
                meterNumber = meter.getMeterNumber();
                List<WaterUsage> usageList = usageRepository.findByMeterMeterIdOrderByYearDescMonthDesc(meter.getMeterId());
                if (!usageList.isEmpty()) {
                    currentUsage = usageList.get(0).getUnitsConsumed();
                }
            }
        } else if (resident.getBuilding() != null && resident.getApartmentNumber() != null && !resident.getApartmentNumber().startsWith("FORMER") && !resident.getApartmentNumber().equals("PENDING")) {
            buildingName = resident.getBuilding().getBuildingName();
            apartmentNumber = resident.getApartmentNumber();
            WaterMeter meter = meterRepository.findByResidentResidentId(resident.getResidentId()).orElse(null);
            if (meter != null) {
                meterNumber = meter.getMeterNumber();
                List<WaterUsage> usageList = usageRepository.findByMeterMeterIdOrderByYearDescMonthDesc(meter.getMeterId());
                if (!usageList.isEmpty()) {
                    currentUsage = usageList.get(0).getUnitsConsumed();
                }
            }
            hasActive = true;
        }

        List<Bill> bills = billRepository.findByResidentResidentIdOrderByGeneratedAtDesc(resident.getResidentId());
        Bill latestBill = bills.isEmpty() ? null : bills.get(0);

        long unreadCount = notificationRepository.findByResidentResidentIdAndIsReadFalse(resident.getResidentId()).size();

        String cycleName = "N/A";
        String startDate = "N/A";
        String endDate = "N/A";

        if (latestBill != null && latestBill.getBillingCycle() != null) {
            BillingCycle cycle = latestBill.getBillingCycle();
            cycleName = cycle.getBillingMonth() != null ? cycle.getBillingMonth().toString() : ("Cycle #" + cycle.getId());
            startDate = cycle.getStartDate() != null ? cycle.getStartDate().toString() : "N/A";
            endDate = cycle.getEndDate() != null ? cycle.getEndDate().toString() : "N/A";
        } else if (latestBill != null) {
            cycleName = latestBill.getBillMonth() + "/" + latestBill.getBillYear();
        }

        return ResidentDashboardStats.builder()
                .residentName(resident.getName())
                .buildingName(buildingName)
                .apartmentNumber(apartmentNumber)
                .meterNumber(meterNumber)
                .currentMonthUsage(currentUsage)
                .activeBillAmount(latestBill != null ? latestBill.getAmount() : 0.0)
                .activeBillStatus(latestBill != null ? latestBill.getStatus().name() : "NO_BILLS")
                .activeBillDueDate(latestBill != null ? latestBill.getDueDate().toString() : "N/A")
                .unreadNotificationsCount(unreadCount)
                .currentBillingCycle(cycleName)
                .cycleStartDate(startDate)
                .cycleEndDate(endDate)
                .hasActiveApartment(hasActive)
                .build();
    }

    public ResidentComparisonResponse getResidentConsumptionComparison(String residentEmail) {
        Resident resident = residentRepository.findByUserEmail(residentEmail)
                .or(() -> residentRepository.findByEmail(residentEmail))
                .orElse(null);

        if (resident == null || resident.getBuilding() == null) {
            return ResidentComparisonResponse.builder()
                    .householdUsage(0.0)
                    .apartmentAverage(0.0)
                    .similarHouseholdAverage(0.0)
                    .differenceFromApartmentAverage(0.0)
                    .percentageDifference(0.0)
                    .message("Please select an active apartment to view water consumption comparisons.")
                    .build();
        }

        Optional<ApartmentOccupancy> activeOcc = occupancyRepository.findActiveByResidentEmail(residentEmail, ApartmentOccupancy.OccupancyStatus.ACTIVE);
        if (activeOcc.isEmpty() && (resident.getApartmentNumber() == null || resident.getApartmentNumber().startsWith("FORMER") || resident.getApartmentNumber().equals("PENDING"))) {
            return ResidentComparisonResponse.builder()
                    .householdUsage(0.0)
                    .apartmentAverage(0.0)
                    .similarHouseholdAverage(0.0)
                    .differenceFromApartmentAverage(0.0)
                    .percentageDifference(0.0)
                    .message("You do not currently have an active apartment occupancy. Please select an apartment to view usage comparisons.")
                    .build();
        }

        WaterMeter meter = meterRepository.findByResidentResidentId(resident.getResidentId()).orElse(null);
        Double householdUsage = 0.0;
        if (meter != null) {
            List<WaterUsage> usageList = usageRepository.findByMeterMeterIdOrderByYearDescMonthDesc(meter.getMeterId());
            if (!usageList.isEmpty()) {
                householdUsage = usageList.get(0).getUnitsConsumed();
            }
        }

        // Get all residents in the building to calculate building average
        List<Resident> buildingResidents = residentRepository.findByBuildingBuildingId(resident.getBuilding().getBuildingId());
        double totalUsage = 0.0;
        int count = 0;

        for (Resident r : buildingResidents) {
            WaterMeter m = meterRepository.findByResidentResidentId(r.getResidentId()).orElse(null);
            if (m != null) {
                List<WaterUsage> uList = usageRepository.findByMeterMeterIdOrderByYearDescMonthDesc(m.getMeterId());
                if (!uList.isEmpty()) {
                    totalUsage += uList.get(0).getUnitsConsumed();
                    count++;
                }
            }
        }

        double apartmentAvg = count > 0 ? Math.round((totalUsage / count) * 100.0) / 100.0 : householdUsage;
        double similarAvg = Math.round((apartmentAvg * 1.02) * 100.0) / 100.0;

        double diff = Math.round((householdUsage - apartmentAvg) * 100.0) / 100.0;
        double pctDiff = apartmentAvg > 0 ? Math.round(((householdUsage - apartmentAvg) / apartmentAvg * 100.0) * 100.0) / 100.0 : 0.0;

        String msg;
        if (householdUsage > apartmentAvg) {
            msg = "Your consumption is above the apartment average. Try reducing your daily water usage to lower your next bill.";
        } else {
            msg = "Great! Your household is using water efficiently compared with the apartment average.";
        }

        return ResidentComparisonResponse.builder()
                .householdUsage(householdUsage)
                .apartmentAverage(apartmentAvg)
                .similarHouseholdAverage(similarAvg)
                .differenceFromApartmentAverage(diff)
                .percentageDifference(pctDiff)
                .message(msg)
                .build();
    }

    @Transactional
    public OwnerUsageComparisonResponse getOwnerUsageComparison(String ownerEmail, Long buildingId) {
        BuildingOwner owner = ownerService.getOwnerByUserEmail(ownerEmail);
        ensureDefaultResidentsForOwner(owner);

        List<Building> buildings = buildingRepository.findByOwnerOwnerId(owner.getOwnerId());
        if (buildings.isEmpty()) {
            return OwnerUsageComparisonResponse.builder()
                    .buildingId(null)
                    .buildingName("N/A")
                    .buildingAverage(0.0)
                    .households(List.of())
                    .build();
        }

        Building selectedBuilding = buildings.get(0);
        if (buildingId != null) {
            for (Building b : buildings) {
                if (b.getBuildingId().equals(buildingId)) {
                    selectedBuilding = b;
                    break;
                }
            }
        }

        List<Resident> residents = residentRepository.findByBuildingBuildingId(selectedBuilding.getBuildingId());
        List<OwnerHouseholdUsageDTO> list = new ArrayList<>();
        double totalUnits = 0.0;
        int count = 0;

        for (Resident r : residents) {
            WaterMeter m = meterRepository.findByResidentResidentId(r.getResidentId()).orElse(null);
            double units = 0.0;
            String meterNum = "N/A";
            if (m != null) {
                meterNum = m.getMeterNumber();
                List<WaterUsage> usageList = usageRepository.findByMeterMeterIdOrderByYearDescMonthDesc(m.getMeterId());
                if (!usageList.isEmpty()) {
                    units = usageList.get(0).getUnitsConsumed();
                }
            }
            totalUnits += units;
            count++;

            list.add(OwnerHouseholdUsageDTO.builder()
                    .residentId(r.getResidentId())
                    .apartmentNumber(r.getApartmentNumber())
                    .residentName(r.getName())
                    .meterNumber(meterNum)
                    .unitsConsumed(units)
                    .build());
        }

        double bAvg = count > 0 ? Math.round((totalUnits / count) * 100.0) / 100.0 : 0.0;

        for (OwnerHouseholdUsageDTO item : list) {
            item.setApartmentAverage(bAvg);
            item.setStatus(item.getUnitsConsumed() > bAvg ? "HIGH" : "NORMAL");
        }

        return OwnerUsageComparisonResponse.builder()
                .buildingId(selectedBuilding.getBuildingId())
                .buildingName(selectedBuilding.getBuildingName())
                .buildingAverage(bAvg)
                .households(list)
                .build();
    }

    public ResidentResponse convertToResidentResponse(Resident resident) {
        WaterMeter meter = meterRepository.findByResidentResidentId(resident.getResidentId()).orElse(null);
        String meterNumber = meter != null ? meter.getMeterNumber() : "N/A";
        Long meterId = meter != null ? meter.getMeterId() : null;
        Long buildingId = resident.getBuilding() != null ? resident.getBuilding().getBuildingId() : null;
        String buildingName = resident.getBuilding() != null ? resident.getBuilding().getBuildingName() : "N/A";

        return ResidentResponse.builder()
                .residentId(resident.getResidentId())
                .buildingId(buildingId)
                .buildingName(buildingName)
                .name(resident.getName())
                .email(resident.getEmail())
                .phone(resident.getPhone())
                .apartmentNumber(resident.getApartmentNumber())
                .invitationToken(resident.getInvitationToken())
                .invitationStatus(resident.getInvitationStatus() != null ? resident.getInvitationStatus().name() : "PENDING")
                .accountStatus(resident.getAccountStatus() != null ? resident.getAccountStatus().name() : "INACTIVE")
                .meterId(meterId)
                .meterNumber(meterNumber)
                .emailSent(false)
                .createdAt(resident.getCreatedAt())
                .build();
    }
}
