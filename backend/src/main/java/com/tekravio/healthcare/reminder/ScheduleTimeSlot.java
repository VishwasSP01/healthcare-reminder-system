package com.tekravio.healthcare.reminder;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "schedule_time_slot")
public class ScheduleTimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private MedicineSchedule schedule;

    @Column(name = "reminder_time", nullable = false)
    private LocalTime reminderTime;

    protected ScheduleTimeSlot() {
    }

    public ScheduleTimeSlot(MedicineSchedule schedule, LocalTime reminderTime) {
        this.schedule = schedule;
        this.reminderTime = reminderTime;
    }

    public Long getId() {
        return id;
    }

    public LocalTime getReminderTime() {
        return reminderTime;
    }
}

