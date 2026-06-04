package com.tekravio.healthcare.reminder.dto;

public record AdherenceResponse(long totalReminders, long takenReminders, double adherencePercentage) {
}

