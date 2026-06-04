package com.tekravio.healthcare.prescription.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record ManualMedicinesRequest(
        @NotEmpty @Size(max = 50) List<@Valid MedicineCandidate> medicines) {
}

