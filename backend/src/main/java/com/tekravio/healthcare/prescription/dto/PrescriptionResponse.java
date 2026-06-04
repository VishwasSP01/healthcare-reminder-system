package com.tekravio.healthcare.prescription.dto;

import java.time.Instant;
import java.util.List;

import com.tekravio.healthcare.prescription.Prescription;
import com.tekravio.healthcare.prescription.PrescriptionStatus;

public record PrescriptionResponse(
        Long id,
        Long patientId,
        String originalFilename,
        String contentType,
        PrescriptionStatus status,
        String s3Key,
        String fileUrl,
        String extractedText,
        Instant uploadedAt,
        List<MedicineResponse> medicines) {

    public static PrescriptionResponse from(Prescription prescription, String fileUrl) {
        return new PrescriptionResponse(
                prescription.getId(),
                prescription.getPatient().getId(),
                prescription.getOriginalFilename(),
                prescription.getContentType(),
                prescription.getStatus(),
                prescription.getS3Key(),
                fileUrl,
                prescription.getExtractedText(),
                prescription.getUploadedAt(),
                prescription.getMedicines().stream().map(MedicineResponse::from).toList());
    }
}

