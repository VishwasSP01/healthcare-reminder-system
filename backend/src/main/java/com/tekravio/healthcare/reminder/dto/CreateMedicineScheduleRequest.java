package com.tekravio.healthcare.reminder.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.tekravio.healthcare.reminder.Frequency;
import com.tekravio.healthcare.reminder.RecurrenceType;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateMedicineScheduleRequest(
        Long medicineId,
        @NotBlank @Size(max = 180) String medicineName,
        @Size(max = 120) String dosage,
        @NotNull Frequency frequency,
        @NotNull RecurrenceType recurrenceType,
        @Min(1) Integer customIntervalHours,
        @NotNull @FutureOrPresent LocalDate startDate,
        LocalDate endDate,
        @NotEmpty @Size(max = 8) List<@NotNull LocalTime> timeSlots) {
}

