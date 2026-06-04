package com.tekravio.healthcare.auth;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

import com.tekravio.healthcare.auth.dto.AuthResponse;
import com.tekravio.healthcare.auth.dto.LoginRequest;
import com.tekravio.healthcare.auth.dto.LogoutRequest;
import com.tekravio.healthcare.auth.dto.RefreshTokenRequest;
import com.tekravio.healthcare.auth.dto.RegisterPatientRequest;
import com.tekravio.healthcare.common.ApiException;
import com.tekravio.healthcare.patient.PatientProfile;
import com.tekravio.healthcare.patient.PatientProfileRepository;
import com.tekravio.healthcare.security.JwtService;
import com.tekravio.healthcare.security.SecurityProperties;
import com.tekravio.healthcare.user.AppUser;
import com.tekravio.healthcare.user.AppUserRepository;
import com.tekravio.healthcare.user.UserRole;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final PatientProfileRepository patientRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final SecurityProperties securityProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            AppUserRepository userRepository,
            PatientProfileRepository patientRepository,
            RefreshTokenRepository refreshTokenRepository,
            BlacklistedTokenRepository blacklistedTokenRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            SecurityProperties securityProperties) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.securityProperties = securityProperties;
    }

    @Transactional
    public AuthResponse registerPatient(RegisterPatientRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }

        AppUser user = userRepository.save(new AppUser(email, passwordEncoder.encode(request.password()), UserRole.PATIENT));
        patientRepository.save(new PatientProfile(
                user,
                request.fullName().trim(),
                request.age(),
                request.medicalHistory(),
                request.whatsappNumber()));

        return tokensFor(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        AppUser user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!user.isActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "User account is inactive");
        }
        return tokensFor(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String hash = JwtService.sha256(request.refreshToken());
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        if (refreshToken.getRevokedAt() != null || refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token is expired or revoked");
        }
        refreshToken.revoke();
        return tokensFor(refreshToken.getUser());
    }

    @Transactional
    public void logout(String accessToken, LogoutRequest request) {
        String refreshHash = JwtService.sha256(request.refreshToken());
        refreshTokenRepository.findByTokenHash(refreshHash).ifPresent(RefreshToken::revoke);

        if (accessToken != null && !accessToken.isBlank()) {
            blacklistedTokenRepository.save(new BlacklistedToken(JwtService.sha256(accessToken), jwtService.expiresAt(accessToken)));
        }
    }

    private AuthResponse tokensFor(AppUser user) {
        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = createOpaqueToken();
        Instant refreshExpiresAt = Instant.now().plusSeconds(securityProperties.refreshTokenDays() * 24 * 60 * 60);
        refreshTokenRepository.save(new RefreshToken(user, JwtService.sha256(refreshToken), refreshExpiresAt));
        return new AuthResponse(accessToken, refreshToken, "Bearer", user.getId(), user.getRole());
    }

    private String createOpaqueToken() {
        byte[] token = new byte[48];
        secureRandom.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }
}

