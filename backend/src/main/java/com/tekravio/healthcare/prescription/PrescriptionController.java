package com.tekravio.healthcare.prescription;

import java.time.Instant;

import com.tekravio.healthcare.prescription.dto.ManualMedicinesRequest;
import com.tekravio.healthcare.prescription.dto.PrescriptionResponse;
import com.tekravio.healthcare.security.AuthPrincipal;
import com.tekravio.healthcare.security.CurrentUser;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/prescriptions")
@PreAuthorize("hasRole('PATIENT')")
class PrescriptionController {

    private final PrescriptionService prescriptionService;

    PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<PrescriptionResponse> upload(
            @CurrentUser AuthPrincipal principal,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(prescriptionService.upload(principal, file));
    }

    @GetMapping
    ResponseEntity<Page<PrescriptionResponse>> history(
            @CurrentUser AuthPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 20, sort = "uploadedAt") Pageable pageable) {
        return ResponseEntity.ok(prescriptionService.history(principal, from, to, pageable));
    }

    @GetMapping("/{prescriptionId}")
    ResponseEntity<PrescriptionResponse> get(
            @CurrentUser AuthPrincipal principal,
            @PathVariable Long prescriptionId) {
        return ResponseEntity.ok(prescriptionService.get(principal, prescriptionId));
    }

    @PutMapping("/{prescriptionId}/medicines")
    ResponseEntity<PrescriptionResponse> replaceManualMedicines(
            @CurrentUser AuthPrincipal principal,
            @PathVariable Long prescriptionId,
            @Valid @RequestBody ManualMedicinesRequest request) {
        return ResponseEntity.ok(prescriptionService.replaceManualMedicines(principal, prescriptionId, request));
    }
}

