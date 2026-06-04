package com.tekravio.healthcare.prescription.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MedicineCandidate(
        @NotBlank @Size(max = 180) String name,
        @Size(max = 120) String dosage,
        @Size(max = 60) String frequency,
        @Size(max = 120) String duration,
        BigDecimal confidence) {
}

