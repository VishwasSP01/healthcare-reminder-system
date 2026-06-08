package com.tekravio.healthcare.audit;

import com.tekravio.healthcare.user.AppUser;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final EntityManager entityManager;

    public AuditLogService(AuditLogRepository auditLogRepository, EntityManager entityManager) {
        this.auditLogRepository = auditLogRepository;
        this.entityManager = entityManager;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long actorUserId, String action, String resourceType, Long resourceId) {
        AppUser actor = actorUserId == null ? null : entityManager.getReference(AppUser.class, actorUserId);
        auditLogRepository.save(new AuditLog(
                actor,
                action,
                resourceType,
                resourceId == null ? null : resourceId.toString()));
    }
}
