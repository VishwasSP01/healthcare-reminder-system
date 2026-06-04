package com.tekravio.healthcare.patient;

import com.tekravio.healthcare.patient.dto.FcmTokenRequest;
import com.tekravio.healthcare.patient.dto.PatientResponse;
import com.tekravio.healthcare.patient.dto.UpdatePatientRequest;
import com.tekravio.healthcare.security.AuthPrincipal;
import com.tekravio.healthcare.security.CurrentUser;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/patients")
class PatientController {

    private final PatientService patientService;

    PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    ResponseEntity<PatientResponse> me(@CurrentUser AuthPrincipal principal) {
        return ResponseEntity.ok(patientService.currentPatient(principal));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    ResponseEntity<PatientResponse> updateMe(
            @CurrentUser AuthPrincipal principal,
            @Valid @RequestBody UpdatePatientRequest request) {
        return ResponseEntity.ok(patientService.updateCurrentPatient(principal, request));
    }

    @PutMapping("/me/fcm-token")
    @PreAuthorize("hasRole('PATIENT')")
    ResponseEntity<Void> updateFcmToken(
            @CurrentUser AuthPrincipal principal,
            @Valid @RequestBody FcmTokenRequest request) {
        patientService.updateFcmToken(principal, request);
        return ResponseEntity.noContent().build();
    }
}

