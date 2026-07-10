package com.neuroforge.nexus.devops.service.impl;

import com.neuroforge.nexus.shared.exception.ResourceNotFoundException;
import com.neuroforge.nexus.shared.util.SecurityUtils;
import com.neuroforge.nexus.devops.domain.*;
import com.neuroforge.nexus.devops.dto.DeploymentRequest;
import com.neuroforge.nexus.devops.dto.DeploymentResponse;
import com.neuroforge.nexus.devops.repository.BuildRepository;
import com.neuroforge.nexus.devops.repository.DeploymentRepository;
import com.neuroforge.nexus.devops.repository.EnvironmentRepository;
import com.neuroforge.nexus.devops.service.AuditEventService;
import com.neuroforge.nexus.devops.service.DeploymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeploymentServiceImpl implements DeploymentService {

    private static final Logger log = LoggerFactory.getLogger(DeploymentServiceImpl.class);
    private final DeploymentRepository deploymentRepository;
    private final EnvironmentRepository environmentRepository;
    private final BuildRepository buildRepository;
    private final AuditEventService auditEventService;

    public DeploymentServiceImpl(DeploymentRepository deploymentRepository, 
                                 EnvironmentRepository environmentRepository, 
                                 BuildRepository buildRepository, 
                                 AuditEventService auditEventService) {
        this.deploymentRepository = deploymentRepository;
        this.environmentRepository = environmentRepository;
        this.buildRepository = buildRepository;
        this.auditEventService = auditEventService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeploymentResponse> getDeploymentsByEnvironment(String environmentId) {
        return deploymentRepository.findByEnvironmentIdOrderByDeployedAtDesc(environmentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeploymentResponse> getProjectDeployments(String projectId) {
        return deploymentRepository.findByBuildPipelineProjectIdOrderByDeployedAtDesc(projectId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DeploymentResponse triggerDeployment(DeploymentRequest request) {
        log.info("Triggering deployment on environment: {}", request.environmentId());
        Environment env = environmentRepository.findById(request.environmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Environment not found: " + request.environmentId()));

        if (!env.isEnabled()) {
            throw new IllegalArgumentException("Target environment '" + env.getName() + "' is disabled.");
        }

        Build build = buildRepository.findById(request.buildId())
                .orElseThrow(() -> new ResourceNotFoundException("Build not found: " + request.buildId()));

        if (!build.getStatus().equalsIgnoreCase("SUCCESS")) {
            throw new IllegalArgumentException("Only successful builds can be deployed.");
        }

        String actor = "SYSTEM";
        try {
            String currentUser = SecurityUtils.getCurrentUsername();
            if (currentUser != null && !currentUser.isBlank()) {
                actor = currentUser;
            }
        } catch (Exception e) {
            log.debug("Unauthenticated context. Defaulting to SYSTEM.");
        }

        auditEventService.logEvent("DEPLOYMENT_STARTED", 
                "Deploying version " + request.version() + " (Build #" + build.getBuildNumber() + ") to environment '" + env.getName() + "'.");

        // Simulate deployment latency
        long durationMs = 1200 + (long)(Math.random() * 800);
        try {
            Thread.sleep(durationMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String logOutput = """
            [Deployer] Connecting to environment host '%s'...
            [Deployer] Copying artifact package: %s
            [Deployer] Extracting and verifying target configuration...
            [Deployer] Starting container services...
            [Deployer] Probing health checks on port 8080...
            [Deployer] Health check: OK. HTTP 200 returned.
            [Deployer] Deployment completed successfully in %dms.
            """.formatted(env.getName().toLowerCase(), build.getArtifactName(), durationMs);

        Deployment deployment = Deployment.builder()
                .id("deploy-" + UUID.randomUUID().toString().substring(0, 8))
                .environment(env)
                .build(build)
                .status("SUCCESSFUL")
                .version(request.version())
                .deployedBy(actor)
                .deployedAt(LocalDateTime.now())
                .durationMs(durationMs)
                .rollbackAvailable(true)
                .deploymentLog(logOutput)
                .build();

        Deployment saved = deploymentRepository.save(deployment);
        auditEventService.logEvent("DEPLOYMENT_COMPLETED", 
                "Successfully deployed version " + request.version() + " to '" + env.getName() + "'.");

        return toResponse(saved);
    }

    @Override
    @Transactional
    public DeploymentResponse rollbackDeployment(String id, String rollbackReason) {
        log.info("Triggering rollback for deployment: {}", id);
        Deployment target = deploymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deployment not found: " + id));

        if (!target.isRollbackAvailable()) {
            throw new IllegalArgumentException("Rollback is not available for this deployment.");
        }

        Environment env = target.getEnvironment();

        // Find the PREVIOUS successful deployment on the same environment
        List<Deployment> history = deploymentRepository.findByEnvironmentIdOrderByDeployedAtDesc(env.getId());
        Deployment rollbackTarget = history.stream()
                .filter(d -> d.getStatus().equalsIgnoreCase("SUCCESSFUL") && !d.getId().equalsIgnoreCase(target.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No previous successful deployment found to roll back to."));

        String actor = "SYSTEM";
        try {
            String currentUser = SecurityUtils.getCurrentUsername();
            if (currentUser != null && !currentUser.isBlank()) {
                actor = currentUser;
            }
        } catch (Exception e) {
            log.debug("Unauthenticated context. Defaulting to SYSTEM.");
        }

        auditEventService.logEvent("ROLLBACK_STARTED", 
                "Rolling back environment '" + env.getName() + "' to previous successful version: " + rollbackTarget.getVersion());

        // Perform Rollback
        target.setStatus("ROLLED_BACK");
        target.setRollbackAvailable(false);
        target.setRollbackReason(rollbackReason != null ? rollbackReason : "Manual Rollback Requested");
        deploymentRepository.save(target);

        // Create new Rollback deployment record
        long durationMs = 800 + (long)(Math.random() * 500);
        String logOutput = """
            [Deployer] Initiating emergency rollback sequence to version %s...
            [Deployer] Re-routing traffic to previously cached deployment container...
            [Deployer] Restarting container hosts...
            [Deployer] Rolling back database schemas...
            [Deployer] Verification checks completed. Restore successful.
            """.formatted(rollbackTarget.getVersion());

        Deployment rollbackDeployment = Deployment.builder()
                .id("deploy-" + UUID.randomUUID().toString().substring(0, 8))
                .environment(env)
                .build(rollbackTarget.getBuild())
                .status("SUCCESSFUL")
                .version(rollbackTarget.getVersion() + "-rolledback")
                .deployedBy(actor)
                .deployedAt(LocalDateTime.now())
                .durationMs(durationMs)
                .rollbackAvailable(false) // rolled back version is not directly rollbacked again
                .rolledBackFromDeploymentId(target.getId())
                .rollbackReason(rollbackReason)
                .deploymentLog(logOutput)
                .build();

        Deployment saved = deploymentRepository.save(rollbackDeployment);
        auditEventService.logEvent("ROLLBACK_COMPLETED", 
                "Environment '" + env.getName() + "' successfully rolled back to version: " + rollbackTarget.getVersion());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DeploymentResponse getDeploymentById(String id) {
        return deploymentRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Deployment not found with id: " + id));
    }

    private DeploymentResponse toResponse(Deployment d) {
        return new DeploymentResponse(
                d.getId(),
                d.getEnvironment().getId(),
                d.getEnvironment().getName(),
                d.getBuild().getId(),
                d.getBuild().getBuildNumber(),
                d.getBuild().getPipeline().getName(),
                d.getStatus(),
                d.getVersion(),
                d.getDeployedBy(),
                d.getDeployedAt(),
                d.getDurationMs(),
                d.isRollbackAvailable(),
                d.getRolledBackFromDeploymentId(),
                d.getRollbackReason(),
                d.getDeploymentLog()
        );
    }
}
