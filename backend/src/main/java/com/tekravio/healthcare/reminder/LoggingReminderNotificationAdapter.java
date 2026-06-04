package com.tekravio.healthcare.reminder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LoggingReminderNotificationAdapter implements ReminderNotificationPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingReminderNotificationAdapter.class);

    @Override
    public void sendMedicineReminder(ReminderLog reminder) {
        log.info(
                "Medicine reminder due: patientId={}, scheduleId={}, medicine={}, dueAt={}",
                reminder.getPatient().getId(),
                reminder.getSchedule().getId(),
                reminder.getSchedule().getMedicineName(),
                reminder.effectiveDueAt());
    }
}
