package com.neuroforge.nexus.devops.dto;

import java.time.LocalDateTime;

public record BuildResponse(
    String id,
    String pipelineId,
    String pipelineName,
    Integer buildNumber,
    String commitId,
    String branch,
    String status,
    String triggerType,
    String triggeredBy,
    LocalDateTime startTime,
    LocalDateTime endTime,
    Long durationMs,
    String artifactName,
    Long artifactSize,
    Integer testsTotal,
    Integer testsPassed,
    Integer testsFailed,
    LocalDateTime createdAt
) {}
