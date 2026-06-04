package com.tekravio.healthcare.reminder;

public interface ReminderNotificationPort {

    void sendMedicineReminder(ReminderLog reminder);
}

