package com.tekravio.healthcare.prescription;

import java.util.List;

import com.tekravio.healthcare.prescription.dto.MedicineCandidate;

public record OcrExtractionResult(
        String provider,
        String rawText,
        List<MedicineCandidate> medicineCandidates,
        boolean lowConfidence) {
}
