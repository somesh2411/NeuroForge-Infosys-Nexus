package com.neuroforge.nexus.sprints.dto;

import java.time.LocalDateTime;

public record SprintResponse(
    String id,
    String projectId,
    String projectName,
    String name,
    String goal,
    LocalDateTime startDate,
    LocalDateTime endDate,
    int capacity,
    String status,
    LocalDateTime createdAt
) {}
