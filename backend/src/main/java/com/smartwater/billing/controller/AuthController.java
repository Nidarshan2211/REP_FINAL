package com.smartwater.billing.controller;

import com.smartwater.billing.dto.AuthDTOs.*;
import com.smartwater.billing.service.AuthService;
import com.smartwater.billing.service.ResidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ResidentService residentService;

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/auth/register-owner")
    public ResponseEntity<MessageResponse> registerOwner(@Valid @RequestBody RegisterOwnerRequest request) {
        return ResponseEntity.ok(authService.registerOwner(request));
    }

    @GetMapping({"/auth/validate-invitation", "/api/residents/invitations/validate"})
    public ResponseEntity<ValidateInvitationResponse> validateInvitation(@RequestParam("token") String token) {
        return ResponseEntity.ok(residentService.validateInvitation(token));
    }

    @PostMapping({"/auth/accept-invitation", "/api/residents/invitations/accept"})
    public ResponseEntity<MessageResponse> acceptInvitation(@Valid @RequestBody SetPasswordRequest request) {
        return ResponseEntity.ok(authService.acceptInvitation(request));
    }
}
