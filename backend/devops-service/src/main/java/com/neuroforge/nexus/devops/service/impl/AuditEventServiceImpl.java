package com.neuroforge.nexus.devops.service.impl;

import com.neuroforge.nexus.shared.util.SecurityUtils;
import com.neuroforge.nexus.devops.domain.AuditEvent;
import com.neuroforge.nexus.devops.dto.AuditEventResponse;
import com.neuroforge.nexus.devops.repository.AuditEventRepository;
import com.neuroforge.nexus.devops.service.AuditEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuditEventServiceImpl implements AuditEventService {

    private static final Logger log = LoggerFactory.getLogger(AuditEventServiceImpl.class);
    private final AuditEventRepository auditEventRepository;

    public AuditEventServiceImpl(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Override
    @Transactional
    public void logEvent(String eventType, String message) {
        String actor = "SYSTEM";
        try {
            String currentUsername = SecurityUtils.getCurrentUsername();
            if (currentUsername != null && !currentUsername.isBlank()) {
                actor = currentUsername;
            }
        } catch (Exception e) {
            log.debug("Unauthenticated audit context. Defaulting to SYSTEM.");
        }

        log.info("DevOps Audit [{}]: {} (Actor: {})", eventType, message, actor);

        AuditEvent event = AuditEvent.builder()
                .id(UUID.randomUUID().toString())
                .eventType(eventType)
                .message(message)
                .actor(actor)
                .timestamp(LocalDateTime.now())
                .build();

        auditEventRepository.save(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEventResponse> getAuditEvents() {
        return auditEventRepository.findTop100ByOrderByTimestampDesc().stream()
                .map(e -> new AuditEventResponse(
                        e.getId(),
                        e.getEventType(),
                        e.getMessage(),
                        e.getActor(),
                        e.getTimestamp()
                ))
                .collect(Collectors.toList());
    }
}
