package com.tekravio.healthcare.reminder;

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
@Table(name = "reminder_log")
public class ReminderLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientProfile patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private MedicineSchedule schedule;

    @Column(name = "due_at", nullable = false)
    private Instant dueAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReminderStatus status;

    @Column(name = "snoozed_until")
    private Instant snoozedUntil;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected ReminderLog() {
    }

    public ReminderLog(PatientProfile patient, MedicineSchedule schedule, Instant dueAt, String idempotencyKey) {
        this.patient = patient;
        this.schedule = schedule;
        this.dueAt = dueAt;
        this.idempotencyKey = idempotencyKey;
        this.status = ReminderStatus.PENDING;
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

    public MedicineSchedule getSchedule() {
        return schedule;
    }

    public Instant getDueAt() {
        return dueAt;
    }

    public ReminderStatus getStatus() {
        return status;
    }

    public void setStatus(ReminderStatus status) {
        this.status = status;
    }

    public Instant getSnoozedUntil() {
        return snoozedUntil;
    }

    public void snoozeUntil(Instant snoozedUntil) {
        this.snoozedUntil = snoozedUntil;
        this.status = ReminderStatus.SNOOZED;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Instant effectiveDueAt() {
        return snoozedUntil == null ? dueAt : snoozedUntil;
    }
}

