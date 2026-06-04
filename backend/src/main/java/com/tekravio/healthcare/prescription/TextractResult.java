package com.tekravio.healthcare.prescription;

import java.util.List;

import com.tekravio.healthcare.prescription.dto.MedicineCandidate;

record TextractResult(String rawText, List<MedicineCandidate> medicineCandidates, boolean lowConfidence) {
}

