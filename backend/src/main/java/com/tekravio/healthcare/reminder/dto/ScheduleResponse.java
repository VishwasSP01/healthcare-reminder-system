package com.tekravio.healthcare.reminder.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.tekravio.healthcare.reminder.Frequency;
import com.tekravio.healthcare.reminder.MedicineSchedule;
import com.tekravio.healthcare.reminder.RecurrenceType;

public record ScheduleResponse(
        Long id,
        Long patientId,
        Long medicineId,
        String medicineName,
        String dosage,
        Frequency frequency,
        RecurrenceType recurrenceType,
        Integer customIntervalHours,
        LocalDate startDate,
        LocalDate endDate,
        boolean active,
        List<LocalTime> timeSlots) {

    public static ScheduleResponse from(MedicineSchedule schedule) {
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getPatient().getId(),
                schedule.getMedicine() == null ? null : schedule.getMedicine().getId(),
                schedule.getMedicineName(),
                schedule.getDosage(),
                schedule.getFrequency(),
                schedule.getRecurrenceType(),
                schedule.getCustomIntervalHours(),
                schedule.getStartDate(),
                schedule.getEndDate(),
                schedule.isActive(),
                schedule.getTimeSlots().stream().map(slot -> slot.getReminderTime()).toList());
    }
}

