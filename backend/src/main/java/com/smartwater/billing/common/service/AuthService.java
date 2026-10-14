package com.smartwater.billing.common.service;

import com.smartwater.billing.common.config.JwtUtils;
import com.smartwater.billing.common.dto.AuthDTOs.*;
import com.smartwater.billing.common.entity.*;
import com.smartwater.billing.owner.entity.*;
import com.smartwater.billing.resident.entity.*;
import com.smartwater.billing.common.exception.BadRequestException;
import com.smartwater.billing.common.exception.ResourceNotFoundException;
import com.smartwater.billing.common.repository.*;
import com.smartwater.billing.owner.repository.*;
import com.smartwater.billing.resident.repository.*;
import com.smartwater.billing.common.util.TokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service

public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final BuildingOwnerRepository ownerRepository;
    private final BuildingRepository buildingRepository;
    private final ResidentRepository residentRepository;
    private final ResidentInvitationRepository invitationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
 
    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       BuildingOwnerRepository ownerRepository,
                       BuildingRepository buildingRepository,
                       ResidentRepository residentRepository,
                       ResidentInvitationRepository invitationRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.ownerRepository = ownerRepository;
        this.buildingRepository = buildingRepository;
        this.residentRepository = residentRepository;
        this.invitationRepository = invitationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (request.getSelectedRole() != null && !request.getSelectedRole().trim().isEmpty()) {
            if (!user.getRole().name().equalsIgnoreCase(request.getSelectedRole().trim())) {
                throw new BadRequestException("Role mismatch: This account is registered as " 
                    + user.getRole().name().replace("_", " ") + ". Please select the correct role tab.");
            }
        }

        if (user.getRole() == User.Role.BUILDING_OWNER) {
            BuildingOwner owner = ownerRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Owner profile not found"));
            if (owner.getApprovalStatus() == BuildingOwner.ApprovalStatus.PENDING) {
                throw new BadRequestException("Your owner account is pending approval by Super Admin.");
            }
            if (owner.getApprovalStatus() == BuildingOwner.ApprovalStatus.REJECTED) {
                throw new BadRequestException("Your owner account registration has been rejected.");
            }
        }

        if (user.getStatus() == User.UserStatus.INACTIVE || user.getStatus() == User.UserStatus.PENDING) {
            throw new BadRequestException("Your account is not active. Please complete setup or wait for approval.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().trim().toLowerCase(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        String phone = "N/A";
        if (user.getRole() == User.Role.BUILDING_OWNER) {
            BuildingOwner owner = ownerRepository.findByUser(user)
                    .or(() -> ownerRepository.findByUserIdOrEmail(user.getUserId(), user.getEmail()))
                    .orElse(null);
            if (owner != null && owner.getPhone() != null && !owner.getPhone().trim().isEmpty()) {
                phone = owner.getPhone();
            }
        } else if (user.getRole() == User.Role.RESIDENT) {
            Resident resident = residentRepository.findByUser(user)
                    .or(() -> residentRepository.findByUserIdOrEmail(user.getUserId(), user.getEmail()))
                    .or(() -> residentRepository.findByEmailIgnoreCase(user.getEmail()))
                    .orElse(null);
            if (resident != null && resident.getPhone() != null && !resident.getPhone().trim().isEmpty()) {
                phone = resident.getPhone();
            }
        }

        return LoginResponse.builder()
                .token(jwt)
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(phone)
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .build();
    }

    @Transactional
    public MessageResponse registerOwner(RegisterOwnerRequest request) {
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BadRequestException("Email is already registered!");
        }

        // Save User
        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.BUILDING_OWNER)
                .status(User.UserStatus.PENDING)
                .build();
        userRepository.save(user);

        // Save Building Owner
        BuildingOwner owner = BuildingOwner.builder()
                .user(user)
                .name(request.getName().trim())
                .phone(request.getPhone().trim())
                .approvalStatus(BuildingOwner.ApprovalStatus.PENDING)
                .build();
        ownerRepository.save(owner);

        // Save Initial Building
        Building building = Building.builder()
                .owner(owner)
                .buildingName(request.getBuildingName().trim())
                .buildingNumber(request.getBuildingNumber().trim())
                .totalFlats(request.getTotalFlats() != null ? request.getTotalFlats() : 10)
                .location(request.getLocation().trim())
                .build();
        buildingRepository.save(building);

        return new MessageResponse("Owner registration successful! Account status is PENDING approval by Super Admin.");
    }

    @Transactional
    public MessageResponse acceptInvitation(SetPasswordRequest request) {
        if (request.getPassword() == null || request.getPassword().trim().length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters long.");
        }

        ResidentInvitation invitation = null;
        Resident resident = null;

        if (request.getToken() != null && !request.getToken().trim().isEmpty()) {
            String rawToken = request.getToken().trim();
            String tokenHash = TokenGenerator.hashToken(rawToken);

            invitation = invitationRepository.findByTokenHash(tokenHash).orElse(null);
            if (invitation != null) {
                resident = invitation.getResident();
            } else {
                resident = residentRepository.findByInvitationToken(rawToken)
                        .orElse(null);
            }
        }

        if (resident == null && request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            String searchEmail = request.getEmail().trim().toLowerCase();
            resident = residentRepository.findByEmailIgnoreCase(searchEmail)
                    .or(() -> residentRepository.findByUserEmail(searchEmail))
                    .orElse(null);
        }

        if (resident == null) {
            throw new ResourceNotFoundException("Invalid or expired invitation token.");
        }

        if (invitation != null && invitation.getStatus() == ResidentInvitation.InvitationStatus.ACCEPTED) {
            throw new BadRequestException("This invitation has already been accepted! Please log in on the Login page.");
        }
        if (resident.getInvitationStatus() == Resident.InvitationStatus.ACCEPTED && resident.getUser() != null) {
            throw new BadRequestException("This resident account (" + resident.getEmail() + ") has already been activated! Please log in on the Login page.");
        }

        LocalDateTime expiresAt = invitation != null ? invitation.getExpiresAt() : resident.getExpiresAt();
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            throw new BadRequestException("This invitation token has expired. Please request a new invitation from your Building Owner.");
        }

        // Create or activate User entity
        User user = resident.getUser();
        if (user == null) {
            user = userRepository.findByEmail(resident.getEmail().toLowerCase()).orElse(null);
        }

        if (user == null) {
            user = User.builder()
                    .name(resident.getName())
                    .email(resident.getEmail().trim().toLowerCase())
                    .password(passwordEncoder.encode(request.getPassword().trim()))
                    .role(User.Role.RESIDENT)
                    .status(User.UserStatus.ACTIVE)
                    .build();
            userRepository.save(user);
        } else {
            user.setPassword(passwordEncoder.encode(request.getPassword().trim()));
            user.setStatus(User.UserStatus.ACTIVE);
            user.setRole(User.Role.RESIDENT);
            userRepository.save(user);
        }

        LocalDateTime now = LocalDateTime.now();

        // Update Resident entity
        resident.setUser(user);
        resident.setInvitationStatus(Resident.InvitationStatus.ACCEPTED);
        resident.setAccountStatus(Resident.AccountStatus.ACTIVE);
        resident.setAcceptedAt(now);
        residentRepository.save(resident);

        // Update ResidentInvitation entity
        if (invitation != null) {
            invitation.setStatus(ResidentInvitation.InvitationStatus.ACCEPTED);
            invitation.setAcceptedAt(now);
            invitationRepository.save(invitation);
        }

        return new MessageResponse("Account for " + resident.getName() + " activated successfully! You can now log in.");
    }
}
