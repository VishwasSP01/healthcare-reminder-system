package com.tekravio.healthcare.reminder;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicineScheduleRepository extends JpaRepository<MedicineSchedule, Long> {

    Page<MedicineSchedule> findByPatientId(Long patientId, Pageable pageable);

    @Query("""
            select s from MedicineSchedule s
            left join fetch s.timeSlots
            where s.active = true
              and s.startDate <= :today
              and (s.endDate is null or s.endDate >= :today)
            """)
    List<MedicineSchedule> findActiveForDate(@Param("today") LocalDate today);
}

