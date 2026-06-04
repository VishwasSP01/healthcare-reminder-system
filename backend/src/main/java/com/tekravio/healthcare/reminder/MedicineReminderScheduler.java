package com.tekravio.healthcare.reminder;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class MedicineReminderScheduler {

    private final MedicineScheduleService scheduleService;

    MedicineReminderScheduler(MedicineScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @Scheduled(fixedDelayString = "${app.reminders.scheduler-delay-ms:300000}")
    void pollDueMedicineReminders() {
        Instant now = Instant.now();
        scheduleService.generateDueReminderLogs(now);
        scheduleService.sendDueReminders(now);
    }
}

