package com.neuroforge.nexus.sprints.dto;

import java.time.LocalDateTime;

public record ActivityLogResponse(
    String id,
    String taskId,
    String sprintId,
    String eventType,
    String message,
    String actor,
    LocalDateTime timestamp
) {}
