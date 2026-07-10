package com.neuroforge.nexus.devops.dto;

import java.time.LocalDateTime;

public record DeploymentResponse(
    String id,
    String environmentId,
    String environmentName,
    String buildId,
    Integer buildNumber,
    String pipelineName,
    String status,
    String version,
    String deployedBy,
    LocalDateTime deployedAt,
    Long durationMs,
    boolean rollbackAvailable,
    String rolledBackFromDeploymentId,
    String rollbackReason,
    String deploymentLog
) {}
