package com.tekravio.healthcare.prescription;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    Page<Prescription> findByPatientId(Long patientId, Pageable pageable);

    Page<Prescription> findByPatientIdAndUploadedAtBetween(Long patientId, Instant from, Instant to, Pageable pageable);
}

