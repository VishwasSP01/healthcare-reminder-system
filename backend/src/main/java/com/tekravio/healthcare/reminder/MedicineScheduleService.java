package com.tekravio.healthcare.reminder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import com.tekravio.healthcare.audit.AuditLogService;
import com.tekravio.healthcare.common.ApiException;
import com.tekravio.healthcare.patient.PatientProfile;
import com.tekravio.healthcare.patient.PatientProfileRepository;
import com.tekravio.healthcare.prescription.Medicine;
import com.tekravio.healthcare.reminder.dto.AdherenceResponse;
import com.tekravio.healthcare.reminder.dto.CreateMedicineScheduleRequest;
import com.tekravio.healthcare.reminder.dto.ReminderResponse;
import com.tekravio.healthcare.reminder.dto.ScheduleResponse;
import com.tekravio.healthcare.reminder.dto.SnoozeRequest;
import com.tekravio.healthcare.security.AuthPrincipal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

@Service
public class MedicineScheduleService {

    private final MedicineScheduleRepository scheduleRepository;
    private final ReminderLogRepository reminderLogRepository;
    private final PatientProfileRepository patientRepository;
    private final ReminderNotificationPort notificationPort;
    private final EntityManager entityManager;
    private final AuditLogService auditLogService;

    public MedicineScheduleService(
            MedicineScheduleRepository scheduleRepository,
            ReminderLogRepository reminderLogRepository,
            PatientProfileRepository patientRepository,
            ReminderNotificationPort notificationPort,
            EntityManager entityManager,
            AuditLogService auditLogService) {
        this.scheduleRepository = scheduleRepository;
        this.reminderLogRepository = reminderLogRepository;
        this.patientRepository = patientRepository;
        this.notificationPort = notificationPort;
        this.entityManager = entityManager;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public ScheduleResponse create(AuthPrincipal principal, CreateMedicineScheduleRequest request) {
        PatientProfile patient = currentPatient(principal);
        validateScheduleRequest(request);
        Medicine medicine = request.medicineId() == null ? null : entityManager.getReference(Medicine.class, request.medicineId());

        MedicineSchedule schedule = new MedicineSchedule(
                patient,
                medicine,
                request.medicineName().trim(),
                request.dosage(),
                request.frequency(),
                request.recurrenceType(),
                request.customIntervalHours(),
                request.startDate(),
                request.endDate());
        schedule.replaceTimeSlots(request.timeSlots().stream()
                .distinct()
                .sorted()
                .map(time -> new ScheduleTimeSlot(schedule, time))
                .toList());

        MedicineSchedule saved = scheduleRepository.save(schedule);
        auditLogService.record(principal.userId(), "MEDICINE_SCHEDULE_CREATED", "MEDICINE_SCHEDULE", saved.getId());
        return ScheduleResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<ScheduleResponse> list(AuthPrincipal principal, Pageable pageable) {
        PatientProfile patient = currentPatient(principal);
        return scheduleRepository.findByPatientId(patient.getId(), pageable).map(ScheduleResponse::from);
    }

    @Transactional
    public ScheduleResponse deactivate(AuthPrincipal principal, Long scheduleId) {
        MedicineSchedule schedule = ownedSchedule(principal, scheduleId);
        schedule.setActive(false);
        auditLogService.record(principal.userId(), "MEDICINE_SCHEDULE_DEACTIVATED", "MEDICINE_SCHEDULE", schedule.getId());
        return ScheduleResponse.from(schedule);
    }

    @Transactional(readOnly = true)
    public Page<ReminderResponse> history(AuthPrincipal principal, Pageable pageable) {
        PatientProfile patient = currentPatient(principal);
        return reminderLogRepository.findByPatientId(patient.getId(), pageable).map(ReminderResponse::from);
    }

    @Transactional
    public ReminderResponse markTaken(AuthPrincipal principal, Long reminderId) {
        ReminderLog reminder = ownedReminder(principal, reminderId);
        reminder.setStatus(ReminderStatus.TAKEN);
        return ReminderResponse.from(reminder);
    }

    @Transactional
    public ReminderResponse markSkipped(AuthPrincipal principal, Long reminderId) {
        ReminderLog reminder = ownedReminder(principal, reminderId);
        reminder.setStatus(ReminderStatus.SKIPPED);
        return ReminderResponse.from(reminder);
    }

    @Transactional
    public ReminderResponse snooze(AuthPrincipal principal, Long reminderId, SnoozeRequest request) {
        if (request.minutes() != 10 && request.minutes() != 20 && request.minutes() != 30) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Snooze must be 10, 20, or 30 minutes");
        }
        ReminderLog reminder = ownedReminder(principal, reminderId);
        reminder.snoozeUntil(Instant.now().plusSeconds(request.minutes() * 60L));
        return ReminderResponse.from(reminder);
    }

    @Transactional(readOnly = true)
    public AdherenceResponse adherence(AuthPrincipal principal) {
        PatientProfile patient = currentPatient(principal);
        Instant to = Instant.now();
        Instant from = to.minusSeconds(7 * 24 * 60 * 60L);
        long total = reminderLogRepository.countByPatientIdAndDueAtBetween(patient.getId(), from, to);
        long taken = reminderLogRepository.countByPatientIdAndStatusAndDueAtBetween(patient.getId(), ReminderStatus.TAKEN, from, to);
        double percentage = total == 0 ? 0.0 : (taken * 100.0) / total;
        return new AdherenceResponse(total, taken, Math.round(percentage * 100.0) / 100.0);
    }

    @Transactional
    public void generateDueReminderLogs(Instant now) {
        LocalDate today = LocalDate.ofInstant(now, ZoneOffset.UTC);
        for (MedicineSchedule schedule : scheduleRepository.findActiveForDate(today)) {
            for (ScheduleTimeSlot slot : schedule.getTimeSlots()) {
                Instant dueAt = LocalDateTime.of(today, slot.getReminderTime()).toInstant(ZoneOffset.UTC);
                if (dueAt.isAfter(now)) {
                    continue;
                }
                String key = schedule.getId() + ":" + dueAt;
                if (!reminderLogRepository.existsByIdempotencyKey(key)) {
                    reminderLogRepository.save(new ReminderLog(schedule.getPatient(), schedule, dueAt, key));
                }
            }
        }
    }

    @Transactional
    public void sendDueReminders(Instant now) {
        List<ReminderLog> due = reminderLogRepository.findDueReminders(List.of(ReminderStatus.PENDING, ReminderStatus.SNOOZED), now);
        for (ReminderLog reminder : due) {
            try {
                notificationPort.sendMedicineReminder(reminder);
                reminder.setStatus(ReminderStatus.SENT);
            } catch (RuntimeException exception) {
                reminder.setStatus(ReminderStatus.FAILED);
            }
        }
    }

    private void validateScheduleRequest(CreateMedicineScheduleRequest request) {
        if (request.endDate() != null && request.endDate().isBefore(request.startDate())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "End date cannot be before start date");
        }
        if (request.recurrenceType() == RecurrenceType.CUSTOM_INTERVAL_HOURS && request.customIntervalHours() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Custom interval hours is required for custom recurrence");
        }
    }

    private PatientProfile currentPatient(AuthPrincipal principal) {
        return patientRepository.findByUserId(principal.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient profile not found"));
    }

    private MedicineSchedule ownedSchedule(AuthPrincipal principal, Long scheduleId) {
        MedicineSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Schedule not found"));
        if (!schedule.getPatient().getUser().getId().equals(principal.userId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Schedule does not belong to current patient");
        }
        return schedule;
    }

    private ReminderLog ownedReminder(AuthPrincipal principal, Long reminderId) {
        ReminderLog reminder = reminderLogRepository.findById(reminderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reminder not found"));
        if (!reminder.getPatient().getUser().getId().equals(principal.userId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Reminder does not belong to current patient");
        }
        return reminder;
    }
}

