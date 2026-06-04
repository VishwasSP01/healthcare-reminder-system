package com.tekravio.healthcare.notification;

import java.util.Map;

import com.tekravio.healthcare.reminder.ReminderLog;
import com.tekravio.healthcare.reminder.ReminderNotificationPort;

import org.springframework.stereotype.Component;

@Component
public class FirebaseReminderNotificationAdapter implements ReminderNotificationPort {

    private final FcmClient fcmClient;
    private final WhatsAppClient whatsAppClient;
    private final NotificationLogRepository notificationLogRepository;
    private final NotificationProperties properties;

    public FirebaseReminderNotificationAdapter(
            FcmClient fcmClient,
            WhatsAppClient whatsAppClient,
            NotificationLogRepository notificationLogRepository,
            NotificationProperties properties) {
        this.fcmClient = fcmClient;
        this.whatsAppClient = whatsAppClient;
        this.notificationLogRepository = notificationLogRepository;
        this.properties = properties;
    }

    @Override
    public void sendMedicineReminder(ReminderLog reminder) {
        String token = reminder.getPatient().getFcmDeviceToken();
        if (token == null || token.isBlank()) {
            logFailure(reminder, "NO_FCM_TOKEN");
            sendWhatsAppFallback(reminder, "No FCM token");
            return;
        }

        FcmMessageRequest request = new FcmMessageRequest(
                token,
                "Medicine reminder",
                "Time to take " + reminder.getSchedule().getMedicineName(),
                Map.of(
                        "patientId", reminder.getPatient().getId().toString(),
                        "scheduleId", reminder.getSchedule().getId().toString(),
                        "action", "TAKE",
                        "medicineName", reminder.getSchedule().getMedicineName()));

        try {
            String response = fcmClient.send(request);
            logSuccess(reminder, response);
        } catch (RuntimeException firstFailure) {
            sleepBeforeRetry();
            try {
                String response = fcmClient.send(request);
                logSuccess(reminder, response);
            } catch (RuntimeException finalFailure) {
                logFailure(reminder, finalFailure.getMessage());
                sendWhatsAppFallback(reminder, finalFailure.getMessage());
            }
        }
    }

    private void logSuccess(ReminderLog reminder, String response) {
        notificationLogRepository.save(new NotificationLog(
                reminder.getPatient(),
                reminder.getSchedule(),
                NotificationChannel.FCM,
                response,
                NotificationStatus.SUCCESS,
                response));
    }

    private void sendWhatsAppFallback(ReminderLog reminder, String reason) {
        String whatsappNumber = reminder.getPatient().getWhatsappNumber();
        if (whatsappNumber == null || whatsappNumber.isBlank()) {
            logWhatsAppFailure(reminder, "NO_WHATSAPP_NUMBER_AFTER_FCM_FAILURE: " + reason);
            throw new IllegalStateException("No FCM token and no WhatsApp number");
        }

        String body = "Medicine reminder: Take "
                + reminder.getSchedule().getMedicineName()
                + (reminder.getSchedule().getDosage() == null ? "" : " " + reminder.getSchedule().getDosage())
                + ". Reply in the app: TAKEN, SNOOZE, or SKIP.";

        try {
            String sid = whatsAppClient.send(new WhatsAppMessageRequest(whatsappNumber, body));
            notificationLogRepository.save(new NotificationLog(
                    reminder.getPatient(),
                    reminder.getSchedule(),
                    NotificationChannel.WHATSAPP,
                    sid,
                    NotificationStatus.SUCCESS,
                    "Fallback after FCM failure: " + reason));
        } catch (RuntimeException exception) {
            logWhatsAppFailure(reminder, exception.getMessage());
            throw exception;
        }
    }

    private void logWhatsAppFailure(ReminderLog reminder, String response) {
        notificationLogRepository.save(new NotificationLog(
                reminder.getPatient(),
                reminder.getSchedule(),
                NotificationChannel.WHATSAPP,
                null,
                NotificationStatus.FAILED,
                response));
    }

    private void logFailure(ReminderLog reminder, String response) {
        notificationLogRepository.save(new NotificationLog(
                reminder.getPatient(),
                reminder.getSchedule(),
                NotificationChannel.FCM,
                null,
                NotificationStatus.FAILED,
                response));
    }

    private void sleepBeforeRetry() {
        if (properties.fcmRetryDelayMs() <= 0) {
            return;
        }
        try {
            Thread.sleep(properties.fcmRetryDelayMs());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted before FCM retry", exception);
        }
    }
}
