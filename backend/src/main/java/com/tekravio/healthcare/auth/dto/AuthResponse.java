package com.tekravio.healthcare.auth.dto;

import com.tekravio.healthcare.user.UserRole;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long userId,
        UserRole role) {
}

