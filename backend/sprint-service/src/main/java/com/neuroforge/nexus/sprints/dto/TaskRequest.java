package com.neuroforge.nexus.sprints.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record TaskRequest(
    @NotBlank(message = "Project ID is required")
    String projectId,

    @NotBlank(message = "Title is required")
    @Size(max = 250, message = "Title cannot exceed 250 characters")
    String title,

    String description,

    String assignedDeveloperId,

    String priority, // LOW, MEDIUM, HIGH, CRITICAL

    String status, // TO_DO, IN_PROGRESS, CODE_REVIEW, TESTING, DONE

    @Min(value = 1, message = "Story points must be at least 1")
    int storyPoints,

    LocalDateTime dueDate,

    String labels,

    Double estimatedHours,

    Double actualHours,

    String sprintId
) {}
