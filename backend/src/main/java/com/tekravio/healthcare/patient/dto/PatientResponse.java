package com.tekravio.healthcare.patient.dto;

import com.tekravio.healthcare.patient.PatientProfile;

public record PatientResponse(
        Long id,
        Long userId,
        String email,
        boolean active,
        String fullName,
        Integer age,
        String medicalHistory,
        boolean hasFcmToken,
        String whatsappNumber) {

    public static PatientResponse from(PatientProfile profile) {
        return new PatientResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getEmail(),
                profile.getUser().isActive(),
                profile.getFullName(),
                profile.getAge(),
                profile.getMedicalHistory(),
                profile.getFcmDeviceToken() != null && !profile.getFcmDeviceToken().isBlank(),
                profile.getWhatsappNumber());
    }
}

