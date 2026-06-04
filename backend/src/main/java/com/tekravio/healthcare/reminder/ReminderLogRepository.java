package com.tekravio.healthcare.reminder;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReminderLogRepository extends JpaRepository<ReminderLog, Long> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    Page<ReminderLog> findByPatientId(Long patientId, Pageable pageable);

    @Query("""
            select r from ReminderLog r
            where r.status in :statuses
              and ((r.snoozedUntil is null and r.dueAt <= :now) or r.snoozedUntil <= :now)
            """)
    List<ReminderLog> findDueReminders(@Param("statuses") Collection<ReminderStatus> statuses, @Param("now") Instant now);

    long countByPatientIdAndDueAtBetween(Long patientId, Instant from, Instant to);

    long countByPatientIdAndStatusAndDueAtBetween(Long patientId, ReminderStatus status, Instant from, Instant to);
}

