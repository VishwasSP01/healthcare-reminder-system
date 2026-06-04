package com.tekravio.healthcare.diet;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import com.tekravio.healthcare.common.ApiException;
import com.tekravio.healthcare.diet.dto.CreateDietPlanRequest;
import com.tekravio.healthcare.diet.dto.DietComplianceResponse;
import com.tekravio.healthcare.diet.dto.DietPlanResponse;
import com.tekravio.healthcare.diet.dto.DietReminderResponse;
import com.tekravio.healthcare.patient.PatientProfile;
import com.tekravio.healthcare.patient.PatientProfileRepository;
import com.tekravio.healthcare.security.AuthPrincipal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DietPlanService {

    private final DietPlanRepository dietPlanRepository;
    private final DietReminderLogRepository dietReminderLogRepository;
    private final PatientProfileRepository patientRepository;
    private final DietReminderNotificationPort notificationPort;

    public DietPlanService(
            DietPlanRepository dietPlanRepository,
            DietReminderLogRepository dietReminderLogRepository,
            PatientProfileRepository patientRepository,
            DietReminderNotificationPort notificationPort) {
        this.dietPlanRepository = dietPlanRepository;
        this.dietReminderLogRepository = dietReminderLogRepository;
        this.patientRepository = patientRepository;
        this.notificationPort = notificationPort;
    }

    @Transactional
    public DietPlanResponse create(CreateDietPlanRequest request) {
        validateDateRange(request.startDate(), request.endDate());
        PatientProfile patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient not found"));
        DietPlan plan = new DietPlan(
                patient,
                request.mealType(),
                request.description().trim(),
                request.scheduledTime(),
                request.dietaryRestrictions(),
                request.calories(),
                request.startDate(),
                request.endDate());
        return DietPlanResponse.from(dietPlanRepository.save(plan), isToday(plan, LocalDate.now(ZoneOffset.UTC)));
    }

    @Transactional
    public DietPlanResponse deactivate(Long dietPlanId) {
        DietPlan plan = dietPlanRepository.findById(dietPlanId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Diet plan not found"));
        plan.setActive(false);
        return DietPlanResponse.from(plan, isToday(plan, LocalDate.now(ZoneOffset.UTC)));
    }

    @Transactional(readOnly = true)
    public Page<DietPlanResponse> listForPatient(Long patientId, Pageable pageable) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        return dietPlanRepository.findByPatientId(patientId, pageable)
                .map(plan -> DietPlanResponse.from(plan, isToday(plan, today)));
    }

    @Transactional(readOnly = true)
    public List<DietPlanResponse> today(AuthPrincipal principal) {
        PatientProfile patient = currentPatient(principal);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        return dietPlanRepository.findTodayForPatient(patient.getId(), today).stream()
                .map(plan -> DietPlanResponse.from(plan, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<DietReminderResponse> reminderHistory(AuthPrincipal principal, Pageable pageable) {
        PatientProfile patient = currentPatient(principal);
        return dietReminderLogRepository.findByPatientId(patient.getId(), pageable).map(DietReminderResponse::from);
    }

    @Transactional
    public DietReminderResponse markEaten(AuthPrincipal principal, Long reminderId) {
        DietReminderLog reminder = ownedReminder(principal, reminderId);
        reminder.setStatus(DietReminderStatus.EATEN);
        return DietReminderResponse.from(reminder);
    }

    @Transactional
    public DietReminderResponse markSkipped(AuthPrincipal principal, Long reminderId) {
        DietReminderLog reminder = ownedReminder(principal, reminderId);
        reminder.setStatus(DietReminderStatus.SKIPPED);
        return DietReminderResponse.from(reminder);
    }

    @Transactional(readOnly = true)
    public DietComplianceResponse compliance(AuthPrincipal principal) {
        PatientProfile patient = currentPatient(principal);
        Instant to = Instant.now();
        Instant from = to.minusSeconds(7 * 24 * 60 * 60L);
        long total = dietReminderLogRepository.countByPatientIdAndDueAtBetween(patient.getId(), from, to);
        long eaten = dietReminderLogRepository.countByPatientIdAndStatusAndDueAtBetween(patient.getId(), DietReminderStatus.EATEN, from, to);
        double percentage = total == 0 ? 0.0 : (eaten * 100.0) / total;
        return new DietComplianceResponse(total, eaten, Math.round(percentage * 100.0) / 100.0);
    }

    @Transactional
    public void generateDueDietReminderLogs(Instant now) {
        LocalDate today = LocalDate.ofInstant(now, ZoneOffset.UTC);
        for (DietPlan plan : dietPlanRepository.findActiveForDate(today)) {
            Instant mealTime = LocalDateTime.of(today, plan.getScheduledTime()).toInstant(ZoneOffset.UTC);
            Instant dueAt = mealTime.minusSeconds(15 * 60L);
            if (dueAt.isAfter(now)) {
                continue;
            }
            String key = plan.getId() + ":" + dueAt;
            if (!dietReminderLogRepository.existsByIdempotencyKey(key)) {
                dietReminderLogRepository.save(new DietReminderLog(plan.getPatient(), plan, dueAt, key));
            }
        }
    }

    @Transactional
    public void sendDueDietReminders(Instant now) {
        List<DietReminderLog> due = dietReminderLogRepository.findDueReminders(List.of(DietReminderStatus.PENDING), now);
        for (DietReminderLog reminder : due) {
            try {
                notificationPort.sendDietReminder(reminder);
                reminder.setStatus(DietReminderStatus.SENT);
            } catch (RuntimeException exception) {
                reminder.setStatus(DietReminderStatus.FAILED);
            }
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "End date cannot be before start date");
        }
    }

    private boolean isToday(DietPlan plan, LocalDate today) {
        return plan.isActive()
                && !plan.getStartDate().isAfter(today)
                && (plan.getEndDate() == null || !plan.getEndDate().isBefore(today));
    }

    private PatientProfile currentPatient(AuthPrincipal principal) {
        return patientRepository.findByUserId(principal.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient profile not found"));
    }

    private DietReminderLog ownedReminder(AuthPrincipal principal, Long reminderId) {
        DietReminderLog reminder = dietReminderLogRepository.findById(reminderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Diet reminder not found"));
        if (!reminder.getPatient().getUser().getId().equals(principal.userId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Diet reminder does not belong to current patient");
        }
        return reminder;
    }
}

