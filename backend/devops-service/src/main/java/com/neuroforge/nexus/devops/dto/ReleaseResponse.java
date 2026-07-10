package com.neuroforge.nexus.devops.dto;

import java.time.LocalDateTime;

public record ReleaseResponse(
    String id,
    String version,
    String name,
    String releaseNotes,
    String buildId,
    Integer buildNumber,
    String pipelineName,
    String status,
    String releasedBy,
    LocalDateTime releasedAt,
    LocalDateTime createdAt
) {}
