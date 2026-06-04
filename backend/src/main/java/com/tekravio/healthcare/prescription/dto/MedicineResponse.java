package com.tekravio.healthcare.prescription.dto;

import java.math.BigDecimal;

import com.tekravio.healthcare.prescription.Medicine;

public record MedicineResponse(
        Long id,
        String name,
        String dosage,
        String frequency,
        String duration,
        BigDecimal confidence,
        boolean manuallyVerified) {

    public static MedicineResponse from(Medicine medicine) {
        return new MedicineResponse(
                medicine.getId(),
                medicine.getName(),
                medicine.getDosage(),
                medicine.getFrequency(),
                medicine.getDuration(),
                medicine.getConfidence(),
                medicine.isManuallyVerified());
    }
}

