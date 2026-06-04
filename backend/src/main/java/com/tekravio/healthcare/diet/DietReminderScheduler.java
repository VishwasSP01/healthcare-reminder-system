package com.tekravio.healthcare.diet;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class DietReminderScheduler {

    private final DietPlanService dietPlanService;

    DietReminderScheduler(DietPlanService dietPlanService) {
        this.dietPlanService = dietPlanService;
    }

    @Scheduled(fixedDelayString = "${app.reminders.scheduler-delay-ms:300000}")
    void pollDueDietReminders() {
        Instant now = Instant.now();
        dietPlanService.generateDueDietReminderLogs(now);
        dietPlanService.sendDueDietReminders(now);
    }
}

