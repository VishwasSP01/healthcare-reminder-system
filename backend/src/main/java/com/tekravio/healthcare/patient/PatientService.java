package com.tekravio.healthcare.patient;

import com.tekravio.healthcare.common.ApiException;
import com.tekravio.healthcare.patient.dto.FcmTokenRequest;
import com.tekravio.healthcare.patient.dto.PatientResponse;
import com.tekravio.healthcare.patient.dto.UpdatePatientRequest;
import com.tekravio.healthcare.security.AuthPrincipal;
import com.tekravio.healthcare.user.UserRole;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {

    private final PatientProfileRepository patientRepository;

    public PatientService(PatientProfileRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional(readOnly = true)
    public PatientResponse currentPatient(AuthPrincipal principal) {
        return PatientResponse.from(currentPatientProfile(principal));
    }

    @Transactional
    public PatientResponse updateCurrentPatient(AuthPrincipal principal, UpdatePatientRequest request) {
        PatientProfile profile = currentPatientProfile(principal);
        profile.setFullName(request.fullName().trim());
        profile.setAge(request.age());
        profile.setMedicalHistory(request.medicalHistory());
        profile.setWhatsappNumber(request.whatsappNumber());
        return PatientResponse.from(profile);
    }

    @Transactional
    public void updateFcmToken(AuthPrincipal principal, FcmTokenRequest request) {
        PatientProfile profile = currentPatientProfile(principal);
        profile.setFcmDeviceToken(request.fcmDeviceToken());
    }

    @Transactional(readOnly = true)
    public Page<PatientResponse> listPatients(String search, Pageable pageable) {
        Page<PatientProfile> page = search == null || search.isBlank()
                ? patientRepository.findAll(pageable)
                : patientRepository.findByFullNameContainingIgnoreCase(search, pageable);
        return page.map(PatientResponse::from);
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatient(Long patientId) {
        return PatientResponse.from(patientRepository.findById(patientId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient not found")));
    }

    @Transactional
    public PatientResponse setPatientActive(Long patientId, boolean active) {
        PatientProfile profile = patientRepository.findById(patientId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient not found"));
        profile.getUser().setActive(active);
        return PatientResponse.from(profile);
    }

    private PatientProfile currentPatientProfile(AuthPrincipal principal) {
        if (principal.role() != UserRole.PATIENT) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only patients can access this resource");
        }
        return patientRepository.findByUserId(principal.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient profile not found"));
    }
}

