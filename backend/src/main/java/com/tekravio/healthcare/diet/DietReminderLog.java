package com.tekravio.healthcare.diet;

import java.time.Instant;

import com.tekravio.healthcare.patient.PatientProfile;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "diet_reminder_log")
public class DietReminderLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientProfile patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "diet_plan_id", nullable = false)
    private DietPlan dietPlan;

    @Column(name = "due_at", nullable = false)
    private Instant dueAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DietReminderStatus status;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected DietReminderLog() {
    }

    public DietReminderLog(PatientProfile patient, DietPlan dietPlan, Instant dueAt, String idempotencyKey) {
        this.patient = patient;
        this.dietPlan = dietPlan;
        this.dueAt = dueAt;
        this.idempotencyKey = idempotencyKey;
        this.status = DietReminderStatus.PENDING;
    }

    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public PatientProfile getPatient() {
        return patient;
    }

    public DietPlan getDietPlan() {
        return dietPlan;
    }

    public Instant getDueAt() {
        return dueAt;
    }

    public DietReminderStatus getStatus() {
        return status;
    }

    public void setStatus(DietReminderStatus status) {
        this.status = status;
    }
}

