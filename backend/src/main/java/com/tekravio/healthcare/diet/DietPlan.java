package com.tekravio.healthcare.diet;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

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
@Table(name = "diet_plan")
public class DietPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientProfile patient;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false)
    private MealType mealType;

    @Column(nullable = false)
    private String description;

    @Column(name = "scheduled_time", nullable = false)
    private LocalTime scheduledTime;

    @Column(name = "dietary_restrictions")
    private String dietaryRestrictions;

    private Integer calories;

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

    protected DietPlan() {
    }

    public DietPlan(
            PatientProfile patient,
            MealType mealType,
            String description,
            LocalTime scheduledTime,
            String dietaryRestrictions,
            Integer calories,
            LocalDate startDate,
            LocalDate endDate) {
        this.patient = patient;
        this.mealType = mealType;
        this.description = description;
        this.scheduledTime = scheduledTime;
        this.dietaryRestrictions = dietaryRestrictions;
        this.calories = calories;
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

    public MealType getMealType() {
        return mealType;
    }

    public String getDescription() {
        return description;
    }

    public LocalTime getScheduledTime() {
        return scheduledTime;
    }

    public String getDietaryRestrictions() {
        return dietaryRestrictions;
    }

    public Integer getCalories() {
        return calories;
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

    public Instant getCreatedAt() {
        return createdAt;
    }
}

