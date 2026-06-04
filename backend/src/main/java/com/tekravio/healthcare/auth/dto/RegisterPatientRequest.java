package com.tekravio.healthcare.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterPatientRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank @Size(max = 160) String fullName,
        @Min(0) @Max(130) Integer age,
        @Size(max = 5000) String medicalHistory,
        @Size(max = 32) String whatsappNumber) {
}

