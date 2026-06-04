package com.tekravio.healthcare.reminder;

import com.tekravio.healthcare.reminder.dto.AdherenceResponse;
import com.tekravio.healthcare.reminder.dto.CreateMedicineScheduleRequest;
import com.tekravio.healthcare.reminder.dto.ReminderResponse;
import com.tekravio.healthcare.reminder.dto.ScheduleResponse;
import com.tekravio.healthcare.reminder.dto.SnoozeRequest;
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
@RequestMapping("/api/v1/medicine-schedules")
@PreAuthorize("hasRole('PATIENT')")
class MedicineScheduleController {

    private final MedicineScheduleService scheduleService;

    MedicineScheduleController(MedicineScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping
    ResponseEntity<ScheduleResponse> create(
            @CurrentUser AuthPrincipal principal,
            @Valid @RequestBody CreateMedicineScheduleRequest request) {
        return ResponseEntity.ok(scheduleService.create(principal, request));
    }

    @GetMapping
    ResponseEntity<Page<ScheduleResponse>> list(
            @CurrentUser AuthPrincipal principal,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(scheduleService.list(principal, pageable));
    }

    @PatchMapping("/{scheduleId}/deactivate")
    ResponseEntity<ScheduleResponse> deactivate(@CurrentUser AuthPrincipal principal, @PathVariable Long scheduleId) {
        return ResponseEntity.ok(scheduleService.deactivate(principal, scheduleId));
    }

    @GetMapping("/reminders")
    ResponseEntity<Page<ReminderResponse>> reminderHistory(
            @CurrentUser AuthPrincipal principal,
            @PageableDefault(size = 20, sort = "dueAt") Pageable pageable) {
        return ResponseEntity.ok(scheduleService.history(principal, pageable));
    }

    @PatchMapping("/reminders/{reminderId}/taken")
    ResponseEntity<ReminderResponse> taken(@CurrentUser AuthPrincipal principal, @PathVariable Long reminderId) {
        return ResponseEntity.ok(scheduleService.markTaken(principal, reminderId));
    }

    @PatchMapping("/reminders/{reminderId}/skipped")
    ResponseEntity<ReminderResponse> skipped(@CurrentUser AuthPrincipal principal, @PathVariable Long reminderId) {
        return ResponseEntity.ok(scheduleService.markSkipped(principal, reminderId));
    }

    @PatchMapping("/reminders/{reminderId}/snooze")
    ResponseEntity<ReminderResponse> snooze(
            @CurrentUser AuthPrincipal principal,
            @PathVariable Long reminderId,
            @Valid @RequestBody SnoozeRequest request) {
        return ResponseEntity.ok(scheduleService.snooze(principal, reminderId, request));
    }

    @GetMapping("/adherence")
    ResponseEntity<AdherenceResponse> adherence(@CurrentUser AuthPrincipal principal) {
        return ResponseEntity.ok(scheduleService.adherence(principal));
    }
}

