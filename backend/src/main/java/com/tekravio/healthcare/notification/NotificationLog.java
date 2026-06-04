package com.tekravio.healthcare.notification;

import java.time.Instant;

import com.tekravio.healthcare.patient.PatientProfile;
import com.tekravio.healthcare.reminder.MedicineSchedule;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "notification_log")
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientProfile patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private MedicineSchedule schedule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Column(name = "provider_message_id")
    private String providerMessageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(name = "response_body")
    private String responseBody;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private Instant sentAt = Instant.now();

    protected NotificationLog() {
    }

    public NotificationLog(
            PatientProfile patient,
            MedicineSchedule schedule,
            NotificationChannel channel,
            String providerMessageId,
            NotificationStatus status,
            String responseBody) {
        this.patient = patient;
        this.schedule = schedule;
        this.channel = channel;
        this.providerMessageId = providerMessageId;
        this.status = status;
        this.responseBody = responseBody;
    }
}

