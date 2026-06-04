package com.tekravio.healthcare.diet;

import java.util.List;

import com.tekravio.healthcare.diet.dto.DietComplianceResponse;
import com.tekravio.healthcare.diet.dto.DietPlanResponse;
import com.tekravio.healthcare.diet.dto.DietReminderResponse;
import com.tekravio.healthcare.security.AuthPrincipal;
import com.tekravio.healthcare.security.CurrentUser;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/diet")
@PreAuthorize("hasRole('PATIENT')")
class PatientDietController {

    private final DietPlanService dietPlanService;

    PatientDietController(DietPlanService dietPlanService) {
        this.dietPlanService = dietPlanService;
    }

    @GetMapping("/today")
    ResponseEntity<List<DietPlanResponse>> today(@CurrentUser AuthPrincipal principal) {
        return ResponseEntity.ok(dietPlanService.today(principal));
    }

    @GetMapping("/reminders")
    ResponseEntity<Page<DietReminderResponse>> reminders(
            @CurrentUser AuthPrincipal principal,
            @PageableDefault(size = 20, sort = "dueAt") Pageable pageable) {
        return ResponseEntity.ok(dietPlanService.reminderHistory(principal, pageable));
    }

    @PatchMapping("/reminders/{reminderId}/eaten")
    ResponseEntity<DietReminderResponse> eaten(@CurrentUser AuthPrincipal principal, @PathVariable Long reminderId) {
        return ResponseEntity.ok(dietPlanService.markEaten(principal, reminderId));
    }

    @PatchMapping("/reminders/{reminderId}/skipped")
    ResponseEntity<DietReminderResponse> skipped(@CurrentUser AuthPrincipal principal, @PathVariable Long reminderId) {
        return ResponseEntity.ok(dietPlanService.markSkipped(principal, reminderId));
    }

    @GetMapping("/compliance")
    ResponseEntity<DietComplianceResponse> compliance(@CurrentUser AuthPrincipal principal) {
        return ResponseEntity.ok(dietPlanService.compliance(principal));
    }
}

