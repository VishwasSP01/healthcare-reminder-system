package com.tekravio.healthcare.diet;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DietReminderLogRepository extends JpaRepository<DietReminderLog, Long> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    Page<DietReminderLog> findByPatientId(Long patientId, Pageable pageable);

    @Query("""
            select r from DietReminderLog r
            where r.status in :statuses
              and r.dueAt <= :now
            """)
    List<DietReminderLog> findDueReminders(@Param("statuses") Collection<DietReminderStatus> statuses, @Param("now") Instant now);

    long countByPatientIdAndDueAtBetween(Long patientId, Instant from, Instant to);

    long countByPatientIdAndStatusAndDueAtBetween(Long patientId, DietReminderStatus status, Instant from, Instant to);
}

