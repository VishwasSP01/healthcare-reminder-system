package com.tekravio.healthcare.reminder.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SnoozeRequest(@Min(10) @Max(30) int minutes) {
}

