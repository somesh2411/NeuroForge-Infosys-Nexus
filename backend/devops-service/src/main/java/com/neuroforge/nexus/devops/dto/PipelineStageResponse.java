package com.neuroforge.nexus.devops.dto;

import java.time.LocalDateTime;

public record PipelineStageResponse(
    String id,
    String buildId,
    String name,
    String status,
    LocalDateTime startTime,
    LocalDateTime endTime,
    Long durationMs,
    String stageLog
) {}
