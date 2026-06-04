package com.tekravio.healthcare.security;

import com.tekravio.healthcare.user.AppUser;
import com.tekravio.healthcare.user.UserRole;

public record AuthPrincipal(Long userId, String email, UserRole role) {

    public static AuthPrincipal from(AppUser user) {
        return new AuthPrincipal(user.getId(), user.getEmail(), user.getRole());
    }
}

