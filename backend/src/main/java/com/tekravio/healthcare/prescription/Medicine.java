package com.tekravio.healthcare.prescription;

import java.math.BigDecimal;
import java.time.Instant;

import com.tekravio.healthcare.patient.PatientProfile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "medicine")
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id")
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientProfile patient;

    @Column(nullable = false)
    private String name;

    private String dosage;

    private String frequency;

    private String duration;

    private BigDecimal confidence;

    @Column(name = "manually_verified", nullable = false)
    private boolean manuallyVerified;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Medicine() {
    }

    public Medicine(
            Prescription prescription,
            PatientProfile patient,
            String name,
            String dosage,
            String frequency,
            String duration,
            BigDecimal confidence,
            boolean manuallyVerified) {
        this.prescription = prescription;
        this.patient = patient;
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
        this.confidence = confidence;
        this.manuallyVerified = manuallyVerified;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDosage() {
        return dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getDuration() {
        return duration;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public boolean isManuallyVerified() {
        return manuallyVerified;
    }
}

