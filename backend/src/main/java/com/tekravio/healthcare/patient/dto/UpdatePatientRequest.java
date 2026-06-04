package com.tekravio.healthcare.patient.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePatientRequest(
        @NotBlank @Size(max = 160) String fullName,
        @Min(0) @Max(130) Integer age,
        @Size(max = 5000) String medicalHistory,
        @Size(max = 32) String whatsappNumber) {
}

