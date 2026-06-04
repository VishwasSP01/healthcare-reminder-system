package com.tekravio.healthcare.reminder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.tekravio.healthcare.patient.PatientProfile;
import com.tekravio.healthcare.prescription.Medicine;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "medicine_schedule")
public class MedicineSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientProfile patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    @Column(name = "medicine_name", nullable = false)
    private String medicineName;

    private String dosage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Frequency frequency;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type", nullable = false)
    private RecurrenceType recurrenceType;

    @Column(name = "custom_interval_hours")
    private Integer customIntervalHours;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleTimeSlot> timeSlots = new ArrayList<>();

    protected MedicineSchedule() {
    }

    public MedicineSchedule(
            PatientProfile patient,
            Medicine medicine,
            String medicineName,
            String dosage,
            Frequency frequency,
            RecurrenceType recurrenceType,
            Integer customIntervalHours,
            LocalDate startDate,
            LocalDate endDate) {
        this.patient = patient;
        this.medicine = medicine;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.recurrenceType = recurrenceType;
        this.customIntervalHours = customIntervalHours;
        this.startDate = startDate;
        this.endDate = endDate;
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

    public Medicine getMedicine() {
        return medicine;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public RecurrenceType getRecurrenceType() {
        return recurrenceType;
    }

    public Integer getCustomIntervalHours() {
        return customIntervalHours;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<ScheduleTimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public void replaceTimeSlots(List<ScheduleTimeSlot> slots) {
        timeSlots.clear();
        timeSlots.addAll(slots);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

