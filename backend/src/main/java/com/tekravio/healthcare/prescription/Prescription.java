package com.tekravio.healthcare.prescription;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.tekravio.healthcare.patient.PatientProfile;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "prescription")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientProfile patient;

    @Column(name = "s3_bucket", nullable = false)
    private String s3Bucket;

    @Column(name = "s3_key", nullable = false, length = 700)
    private String s3Key;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrescriptionStatus status = PrescriptionStatus.PENDING;

    @Column(name = "ocr_provider", nullable = false)
    private String ocrProvider = "textract";

    @Column(name = "extracted_text")
    private String extractedText;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt = Instant.now();

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Medicine> medicines = new ArrayList<>();

    protected Prescription() {
    }

    public Prescription(PatientProfile patient, String s3Bucket, String s3Key, String originalFilename, String contentType) {
        this.patient = patient;
        this.s3Bucket = s3Bucket;
        this.s3Key = s3Key;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
    }

    public Long getId() {
        return id;
    }

    public PatientProfile getPatient() {
        return patient;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public String getS3Key() {
        return s3Key;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public PrescriptionStatus getStatus() {
        return status;
    }

    public void setStatus(PrescriptionStatus status) {
        this.status = status;
    }

    public String getOcrProvider() {
        return ocrProvider;
    }

    public void setOcrProvider(String ocrProvider) {
        this.ocrProvider = ocrProvider;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public List<Medicine> getMedicines() {
        return medicines;
    }

    public void replaceMedicines(List<Medicine> newMedicines) {
        medicines.clear();
        medicines.addAll(newMedicines);
    }
}
