package com.tekravio.healthcare.diet.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.tekravio.healthcare.diet.MealType;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateDietPlanRequest(
        @NotNull Long patientId,
        @NotNull MealType mealType,
        @NotBlank @Size(max = 5000) String description,
        @NotNull LocalTime scheduledTime,
        @Size(max = 2000) String dietaryRestrictions,
        @Min(0) Integer calories,
        @NotNull @FutureOrPresent LocalDate startDate,
        LocalDate endDate) {
}

