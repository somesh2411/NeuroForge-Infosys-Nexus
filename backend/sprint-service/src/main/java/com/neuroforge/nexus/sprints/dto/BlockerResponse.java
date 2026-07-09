package com.neuroforge.nexus.sprints.dto;

import java.time.LocalDateTime;

public record BlockerResponse(
    String id,
    String taskId,
    String name,
    String status,
    LocalDateTime resolvedAt,
    String resolvedBy,
    LocalDateTime createdAt,
    String createdBy
) {}
