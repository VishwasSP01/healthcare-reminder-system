package com.tekravio.healthcare.auth;

import com.tekravio.healthcare.auth.dto.AuthResponse;
import com.tekravio.healthcare.auth.dto.LoginRequest;
import com.tekravio.healthcare.auth.dto.LogoutRequest;
import com.tekravio.healthcare.auth.dto.RefreshTokenRequest;
import com.tekravio.healthcare.auth.dto.RegisterPatientRequest;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
class AuthController {

    private final AuthService authService;

    AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterPatientRequest request) {
        return ResponseEntity.ok(authService.registerPatient(request));
    }

    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody LogoutRequest request) {
        String accessToken = authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7) : null;
        authService.logout(accessToken, request);
        return ResponseEntity.noContent().build();
    }
}

