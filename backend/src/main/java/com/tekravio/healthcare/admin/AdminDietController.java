package com.tekravio.healthcare.admin;

import com.tekravio.healthcare.diet.DietPlanService;
import com.tekravio.healthcare.diet.dto.CreateDietPlanRequest;
import com.tekravio.healthcare.diet.dto.DietPlanResponse;
import com.tekravio.healthcare.security.AuthPrincipal;
import com.tekravio.healthcare.security.CurrentUser;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/diet-plans")
@PreAuthorize("hasRole('ADMIN')")
class AdminDietController {

    private final DietPlanService dietPlanService;

    AdminDietController(DietPlanService dietPlanService) {
        this.dietPlanService = dietPlanService;
    }

    @PostMapping
    ResponseEntity<DietPlanResponse> create(
            @CurrentUser AuthPrincipal principal,
            @Valid @RequestBody CreateDietPlanRequest request) {
        return ResponseEntity.ok(dietPlanService.create(principal, request));
    }

    @GetMapping("/patient/{patientId}")
    ResponseEntity<Page<DietPlanResponse>> listForPatient(
            @PathVariable Long patientId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(dietPlanService.listForPatient(patientId, pageable));
    }

    @PatchMapping("/{dietPlanId}/deactivate")
    ResponseEntity<DietPlanResponse> deactivate(@CurrentUser AuthPrincipal principal, @PathVariable Long dietPlanId) {
        return ResponseEntity.ok(dietPlanService.deactivate(principal, dietPlanId));
    }
}

