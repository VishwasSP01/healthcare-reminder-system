package com.tekravio.healthcare.diet;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DietPlanRepository extends JpaRepository<DietPlan, Long> {

    Page<DietPlan> findByPatientId(Long patientId, Pageable pageable);

    @Query("""
            select d from DietPlan d
            where d.patient.id = :patientId
              and d.active = true
              and d.startDate <= :today
              and (d.endDate is null or d.endDate >= :today)
            order by d.scheduledTime
            """)
    List<DietPlan> findTodayForPatient(@Param("patientId") Long patientId, @Param("today") LocalDate today);

    @Query("""
            select d from DietPlan d
            where d.active = true
              and d.startDate <= :today
              and (d.endDate is null or d.endDate >= :today)
            """)
    List<DietPlan> findActiveForDate(@Param("today") LocalDate today);
}

