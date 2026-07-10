package com.neuroforge.nexus.devops.service.impl;

import com.neuroforge.nexus.shared.exception.ResourceNotFoundException;
import com.neuroforge.nexus.devops.domain.Environment;
import com.neuroforge.nexus.devops.dto.EnvironmentRequest;
import com.neuroforge.nexus.devops.dto.EnvironmentResponse;
import com.neuroforge.nexus.devops.repository.EnvironmentRepository;
import com.neuroforge.nexus.devops.service.AuditEventService;
import com.neuroforge.nexus.devops.service.EnvironmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EnvironmentServiceImpl implements EnvironmentService {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentServiceImpl.class);
    private final EnvironmentRepository environmentRepository;
    private final AuditEventService auditEventService;

    public EnvironmentServiceImpl(EnvironmentRepository environmentRepository, AuditEventService auditEventService) {
        this.environmentRepository = environmentRepository;
        this.auditEventService = auditEventService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnvironmentResponse> getAllEnvironments() {
        return environmentRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EnvironmentResponse getEnvironmentById(String id) {
        return environmentRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Environment not found with id: " + id));
    }

    @Override
    @Transactional
    public EnvironmentResponse createEnvironment(EnvironmentRequest request) {
        log.info("Creating deployment environment: {}", request.name());
        
        Environment env = Environment.builder()
                .id("env-" + UUID.randomUUID().toString().substring(0, 8))
                .name(request.name())
                .description(request.description())
                .enabled(request.enabled())
                .build();

        Environment saved = environmentRepository.save(env);
        auditEventService.logEvent("ENVIRONMENT_CREATED", "Deployment environment '" + env.getName() + "' created.");
        return toResponse(saved);
    }

    @Override
    @Transactional
    public EnvironmentResponse updateEnvironment(String id, EnvironmentRequest request) {
        log.info("Updating deployment environment: {}", id);
        Environment env = environmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Environment not found with id: " + id));

        env.setName(request.name());
        env.setDescription(request.description());
        env.setEnabled(request.enabled());

        Environment saved = environmentRepository.save(env);
        auditEventService.logEvent("ENVIRONMENT_UPDATED", "Deployment environment '" + env.getName() + "' updated.");
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteEnvironment(String id) {
        log.info("Deleting deployment environment: {}", id);
        Environment env = environmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Environment not found with id: " + id));

        environmentRepository.delete(env);
        auditEventService.logEvent("ENVIRONMENT_DELETED", "Deployment environment '" + env.getName() + "' deleted.");
    }

    @Override
    @Transactional
    public EnvironmentResponse toggleEnvironmentStatus(String id, boolean enabled) {
        log.info("Toggling deployment environment {} status to {}", id, enabled);
        Environment env = environmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Environment not found with id: " + id));

        env.setEnabled(enabled);
        Environment saved = environmentRepository.save(env);
        auditEventService.logEvent("ENVIRONMENT_STATE_CHANGED", 
                "Deployment environment '" + env.getName() + "' is now " + (enabled ? "ENABLED" : "DISABLED"));
        return toResponse(saved);
    }

    private EnvironmentResponse toResponse(Environment env) {
        return new EnvironmentResponse(
                env.getId(),
                env.getName(),
                env.getDescription(),
                env.isEnabled(),
                env.getCreatedAt(),
                env.getCreatedBy()
        );
    }
}
