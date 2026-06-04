package com.tekravio.healthcare.reminder.dto;

import java.time.Instant;

import com.tekravio.healthcare.reminder.ReminderLog;
import com.tekravio.healthcare.reminder.ReminderStatus;

public record ReminderResponse(
        Long id,
        Long scheduleId,
        String medicineName,
        String dosage,
        Instant dueAt,
        Instant snoozedUntil,
        ReminderStatus status) {

    public static ReminderResponse from(ReminderLog reminder) {
        return new ReminderResponse(
                reminder.getId(),
                reminder.getSchedule().getId(),
                reminder.getSchedule().getMedicineName(),
                reminder.getSchedule().getDosage(),
                reminder.getDueAt(),
                reminder.getSnoozedUntil(),
                reminder.getStatus());
    }
}

