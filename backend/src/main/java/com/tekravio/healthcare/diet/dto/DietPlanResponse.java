package com.tekravio.healthcare.diet.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.tekravio.healthcare.diet.DietPlan;
import com.tekravio.healthcare.diet.MealType;

public record DietPlanResponse(
        Long id,
        Long patientId,
        MealType mealType,
        String description,
        LocalTime scheduledTime,
        String dietaryRestrictions,
        Integer calories,
        LocalDate startDate,
        LocalDate endDate,
        boolean active,
        boolean today) {

    public static DietPlanResponse from(DietPlan plan, boolean today) {
        return new DietPlanResponse(
                plan.getId(),
                plan.getPatient().getId(),
                plan.getMealType(),
                plan.getDescription(),
                plan.getScheduledTime(),
                plan.getDietaryRestrictions(),
                plan.getCalories(),
                plan.getStartDate(),
                plan.getEndDate(),
                plan.isActive(),
                today);
    }
}

