package com.tekravio.healthcare.admin;

import com.tekravio.healthcare.patient.PatientService;
import com.tekravio.healthcare.patient.dto.PatientResponse;
import com.tekravio.healthcare.prescription.PrescriptionService;
import com.tekravio.healthcare.prescription.dto.PrescriptionResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/patients")
@PreAuthorize("hasRole('ADMIN')")
class AdminPatientController {

    private final PatientService patientService;
    private final PrescriptionService prescriptionService;

    AdminPatientController(PatientService patientService, PrescriptionService prescriptionService) {
        this.patientService = patientService;
        this.prescriptionService = prescriptionService;
    }

    @GetMapping
    ResponseEntity<Page<PatientResponse>> listPatients(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(patientService.listPatients(search, pageable));
    }

    @GetMapping("/{patientId}")
    ResponseEntity<PatientResponse> getPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(patientService.getPatient(patientId));
    }

    @GetMapping("/{patientId}/prescriptions")
    ResponseEntity<Page<PrescriptionResponse>> getPatientPrescriptions(
            @PathVariable Long patientId,
            @PageableDefault(size = 20, sort = "uploadedAt") Pageable pageable) {
        return ResponseEntity.ok(prescriptionService.historyForPatient(patientId, pageable));
    }

    @PatchMapping("/{patientId}/activate")
    ResponseEntity<PatientResponse> activatePatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(patientService.setPatientActive(patientId, true));
    }

    @PatchMapping("/{patientId}/deactivate")
    ResponseEntity<PatientResponse> deactivatePatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(patientService.setPatientActive(patientId, false));
    }
}

