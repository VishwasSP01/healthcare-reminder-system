package com.tekravio.healthcare.diet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class LoggingDietReminderNotificationAdapter implements DietReminderNotificationPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingDietReminderNotificationAdapter.class);

    @Override
    public void sendDietReminder(DietReminderLog reminder) {
        log.info(
                "Diet reminder due: patientId={}, dietPlanId={}, meal={}, dueAt={}",
                reminder.getPatient().getId(),
                reminder.getDietPlan().getId(),
                reminder.getDietPlan().getMealType(),
                reminder.getDueAt());
    }
}

