package com.neuroforge.nexus.sprints.dto;

import java.time.LocalDateTime;

public record TaskResponse(
    String id,
    String projectId,
    String sprintId,
    String sprintName,
    String title,
    String description,
    String assignedDeveloperId,
    String assignedDeveloperName,
    String priority,
    String status,
    int storyPoints,
    LocalDateTime dueDate,
    String labels,
    Double estimatedHours,
    Double actualHours,
    int version,
    LocalDateTime createdAt,
    String createdBy,
    LocalDateTime updatedAt,
    String updatedBy
) {}
