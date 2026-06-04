package com.tekravio.healthcare.diet.dto;

import java.time.Instant;

import com.tekravio.healthcare.diet.DietReminderLog;
import com.tekravio.healthcare.diet.DietReminderStatus;
import com.tekravio.healthcare.diet.MealType;

public record DietReminderResponse(
        Long id,
        Long dietPlanId,
        MealType mealType,
        String description,
        Instant dueAt,
        DietReminderStatus status) {

    public static DietReminderResponse from(DietReminderLog reminder) {
        return new DietReminderResponse(
                reminder.getId(),
                reminder.getDietPlan().getId(),
                reminder.getDietPlan().getMealType(),
                reminder.getDietPlan().getDescription(),
                reminder.getDueAt(),
                reminder.getStatus());
    }
}

