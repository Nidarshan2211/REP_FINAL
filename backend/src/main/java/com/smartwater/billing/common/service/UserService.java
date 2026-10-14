package com.smartwater.billing.common.service;

import com.smartwater.billing.common.dto.AuthDTOs.MessageResponse;
import com.smartwater.billing.common.dto.UserDTOs.*;
import com.smartwater.billing.common.entity.*;
import com.smartwater.billing.owner.entity.*;
import com.smartwater.billing.resident.entity.*;
import com.smartwater.billing.common.exception.BadRequestException;
import com.smartwater.billing.common.exception.ResourceNotFoundException;
import com.smartwater.billing.common.repository.*;
import com.smartwater.billing.owner.repository.*;
import com.smartwater.billing.resident.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BuildingOwnerRepository ownerRepository;
    private final ResidentRepository residentRepository;
    private final ResidentInvitationRepository invitationRepository;
    private final BuildingRepository buildingRepository;
    private final WaterMeterRepository meterRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BuildingOwnerRepository ownerRepository, ResidentRepository residentRepository, ResidentInvitationRepository invitationRepository, BuildingRepository buildingRepository, WaterMeterRepository meterRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.ownerRepository = ownerRepository;
        this.residentRepository = residentRepository;
        this.invitationRepository = invitationRepository;
        this.buildingRepository = buildingRepository;
        this.meterRepository = meterRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public UserProfileResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found for email: " + email));

        UserProfileResponse.Builder builder = UserProfileResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt());

        if (user.getRole() == User.Role.RESIDENT) {
            Resident resident = residentRepository.findByUser(user)
                    .or(() -> residentRepository.findByUserIdOrEmail(user.getUserId(), user.getEmail()))
                    .or(() -> residentRepository.findByEmailIgnoreCase(user.getEmail()))
                    .orElse(null);

            if (resident != null) {
                WaterMeter meter = meterRepository.findByResidentResidentId(resident.getResidentId()).orElse(null);
                String phone = resident.getPhone();

                // If phone is missing on resident record, check ResidentInvitation
                if (phone == null || phone.trim().isEmpty() || "N/A".equalsIgnoreCase(phone.trim())) {
                    List<ResidentInvitation> invites = invitationRepository.findByResidentEmailOrderByCreatedAtDesc(resident.getEmail());
                    if (!invites.isEmpty() && invites.get(0).getPhoneNumber() != null && !invites.get(0).getPhoneNumber().trim().isEmpty()) {
                        phone = invites.get(0).getPhoneNumber().trim();
                        resident.setPhone(phone);
                        residentRepository.save(resident);
                    }
                }

                builder.residentId(resident.getResidentId())
                        .phone(phone != null && !phone.trim().isEmpty() ? phone : "N/A")
                        .apartmentNumber(resident.getApartmentNumber())
                        .buildingName(resident.getBuilding() != null ? resident.getBuilding().getBuildingName() : "N/A")
                        .meterNumber(meter != null ? meter.getMeterNumber() : "WM-DEFAULT");
            }
        } else if (user.getRole() == User.Role.BUILDING_OWNER) {
            BuildingOwner owner = ownerRepository.findByUser(user)
                    .or(() -> ownerRepository.findByUserIdOrEmail(user.getUserId(), user.getEmail()))
                    .orElse(null);

            if (owner != null) {
                List<Building> buildings = buildingRepository.findByOwnerOwnerId(owner.getOwnerId());
                int residentCount = residentRepository.findByBuildingOwnerOwnerId(owner.getOwnerId()).size();
                String phone = owner.getPhone();

                builder.ownerId(owner.getOwnerId())
                        .phone(phone != null && !phone.trim().isEmpty() ? phone : "N/A")
                        .totalBuildings(buildings.size())
                        .totalResidents(residentCount);
            }
        } else if (user.getRole() == User.Role.SUPER_ADMIN) {
            builder.systemPrivilege("Full System Control & Owner Approvals")
                    .phone("+1 800-AQUA-FLOW");
        }

        return builder.build();
    }

    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }
        userRepository.save(user);

        if (user.getRole() == User.Role.RESIDENT) {
            Resident resident = residentRepository.findByUser(user)
                    .or(() -> residentRepository.findByUserIdOrEmail(user.getUserId(), user.getEmail()))
                    .or(() -> residentRepository.findByEmailIgnoreCase(user.getEmail()))
                    .orElse(null);
            if (resident != null) {
                resident.setName(user.getName());
                if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
                    resident.setPhone(request.getPhone().trim());
                }
                residentRepository.save(resident);
            }
        } else if (user.getRole() == User.Role.BUILDING_OWNER) {
            BuildingOwner owner = ownerRepository.findByUser(user)
                    .or(() -> ownerRepository.findByUserIdOrEmail(user.getUserId(), user.getEmail()))
                    .orElse(null);
            if (owner != null) {
                owner.setName(user.getName());
                if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
                    owner.setPhone(request.getPhone().trim());
                }
                ownerRepository.save(owner);
            }
        }

        return getUserProfile(user.getEmail());
    }

    @Transactional
    public MessageResponse changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password does not match!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
        userRepository.save(user);

        return new MessageResponse("Password updated successfully!");
    }
}
