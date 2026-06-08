package com.tekravio.healthcare.prescription;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.tekravio.healthcare.audit.AuditLogService;
import com.tekravio.healthcare.aws.AwsProperties;
import com.tekravio.healthcare.common.ApiException;
import com.tekravio.healthcare.patient.PatientProfile;
import com.tekravio.healthcare.patient.PatientProfileRepository;
import com.tekravio.healthcare.prescription.dto.ManualMedicinesRequest;
import com.tekravio.healthcare.prescription.dto.MedicineCandidate;
import com.tekravio.healthcare.prescription.dto.PrescriptionResponse;
import com.tekravio.healthcare.security.AuthPrincipal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PrescriptionService {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionService.class);
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "application/pdf");

    private final PrescriptionRepository prescriptionRepository;
    private final PatientProfileRepository patientRepository;
    private final S3PrescriptionStorage storage;
    private final PrescriptionOcrRouter ocrRouter;
    private final AwsProperties awsProperties;
    private final AuditLogService auditLogService;

    public PrescriptionService(
            PrescriptionRepository prescriptionRepository,
            PatientProfileRepository patientRepository,
            S3PrescriptionStorage storage,
            PrescriptionOcrRouter ocrRouter,
            AwsProperties awsProperties,
            AuditLogService auditLogService) {
        this.prescriptionRepository = prescriptionRepository;
        this.patientRepository = patientRepository;
        this.storage = storage;
        this.ocrRouter = ocrRouter;
        this.awsProperties = awsProperties;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public PrescriptionResponse upload(AuthPrincipal principal, MultipartFile file) {
        PatientProfile patient = patientRepository.findByUserId(principal.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient profile not found"));

        validateUpload(file);
        String key = buildS3Key(patient.getId(), file);

        Prescription prescription = new Prescription(
                patient,
                awsProperties.s3Bucket(),
                key,
                safeFilename(file.getOriginalFilename()),
                file.getContentType());
        prescription = prescriptionRepository.save(prescription);

        try {
            storage.upload(key, file);
        } catch (IOException exception) {
            log.warn("Could not read prescription file {}", prescription.getOriginalFilename(), exception);
            prescription.setStatus(PrescriptionStatus.FAILED);
            prescription.setExtractedText("Could not read prescription file. Please upload a clear JPG, PNG, or PDF again.");
            Prescription saved = prescriptionRepository.save(prescription);
            auditLogService.record(principal.userId(), "PRESCRIPTION_UPLOAD_FAILED", "PRESCRIPTION", saved.getId());
            return toResponse(saved);
        } catch (RuntimeException exception) {
            log.warn("Could not upload prescription {} to S3", prescription.getOriginalFilename(), exception);
            prescription.setStatus(PrescriptionStatus.FAILED);
            prescription.setExtractedText("Storage upload failed. Check AWS credentials, bucket permissions, and region.");
            Prescription saved = prescriptionRepository.save(prescription);
            auditLogService.record(principal.userId(), "PRESCRIPTION_UPLOAD_FAILED", "PRESCRIPTION", saved.getId());
            return toResponse(saved);
        }

        try {
            OcrExtractionResult result = ocrRouter.extract(awsProperties.s3Bucket(), key, file.getContentType());
            prescription.setOcrProvider(result.provider());
            prescription.setExtractedText(result.rawText());
            prescription.replaceMedicines(toMedicines(prescription, patient, result.medicineCandidates(), false));
            prescription.setStatus(result.lowConfidence() || result.medicineCandidates().isEmpty()
                    ? PrescriptionStatus.MANUAL_REVIEW
                    : PrescriptionStatus.DONE);
        } catch (RuntimeException exception) {
            log.warn("OCR extraction failed for prescription {}", prescription.getId(), exception);
            prescription.setStatus(PrescriptionStatus.MANUAL_REVIEW);
            prescription.setExtractedText("OCR extraction failed. Manual medicine review is required before creating reminders.");
        }

        Prescription saved = prescriptionRepository.save(prescription);
        auditLogService.record(principal.userId(), "PRESCRIPTION_UPLOAD", "PRESCRIPTION", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<PrescriptionResponse> history(AuthPrincipal principal, Instant from, Instant to, Pageable pageable) {
        PatientProfile patient = patientRepository.findByUserId(principal.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient profile not found"));
        Page<Prescription> page = from != null && to != null
                ? prescriptionRepository.findByPatientIdAndUploadedAtBetween(patient.getId(), from, to, pageable)
                : prescriptionRepository.findByPatientId(patient.getId(), pageable);
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PrescriptionResponse> historyForPatient(Long patientId, Pageable pageable) {
        if (!patientRepository.existsById(patientId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Patient profile not found");
        }
        return prescriptionRepository.findByPatientId(patientId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse get(AuthPrincipal principal, Long prescriptionId) {
        Prescription prescription = ownedPrescription(principal, prescriptionId);
        return toResponse(prescription);
    }

    @Transactional
    public PrescriptionResponse replaceManualMedicines(AuthPrincipal principal, Long prescriptionId, ManualMedicinesRequest request) {
        Prescription prescription = ownedPrescription(principal, prescriptionId);
        prescription.replaceMedicines(toMedicines(prescription, prescription.getPatient(), request.medicines(), true));
        prescription.setStatus(PrescriptionStatus.DONE);
        auditLogService.record(principal.userId(), "PRESCRIPTION_MEDICINES_REVIEWED", "PRESCRIPTION", prescription.getId());
        return toResponse(prescription);
    }

    private Prescription ownedPrescription(AuthPrincipal principal, Long prescriptionId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Prescription not found"));
        if (!prescription.getPatient().getUser().getId().equals(principal.userId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Prescription does not belong to current patient");
        }
        return prescription;
    }

    private void validateUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Prescription file is required");
        }
        if (file.getSize() > awsProperties.maxPrescriptionUploadBytes()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Prescription file must be 10MB or smaller");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only JPG, PNG, and PDF prescription files are allowed");
        }
    }

    private String buildS3Key(Long patientId, MultipartFile file) {
        return "%s/%d/%s.%s".formatted(
                awsProperties.s3PrescriptionPrefix(),
                patientId,
                UUID.randomUUID(),
                extension(file));
    }

    private String extension(MultipartFile file) {
        return switch (file.getContentType()) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "application/pdf" -> "pdf";
            default -> "bin";
        };
    }

    private String safeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "prescription";
        }
        return filename.replaceAll("[^A-Za-z0-9._ -]", "_");
    }

    private List<Medicine> toMedicines(
            Prescription prescription,
            PatientProfile patient,
            List<MedicineCandidate> candidates,
            boolean manuallyVerified) {
        return candidates.stream()
                .map(candidate -> new Medicine(
                        prescription,
                        patient,
                        candidate.name().trim(),
                        candidate.dosage(),
                        candidate.frequency(),
                        candidate.duration(),
                        candidate.confidence() == null ? BigDecimal.valueOf(100) : candidate.confidence(),
                        manuallyVerified))
                .toList();
    }

    private PrescriptionResponse toResponse(Prescription prescription) {
        return PrescriptionResponse.from(prescription, safePresignedUrl(prescription));
    }

    private String safePresignedUrl(Prescription prescription) {
        try {
            return storage.presignedUrl(prescription.getS3Key());
        } catch (RuntimeException exception) {
            log.warn("Could not create pre-signed URL for prescription {}", prescription.getId(), exception);
            return null;
        }
    }
}
