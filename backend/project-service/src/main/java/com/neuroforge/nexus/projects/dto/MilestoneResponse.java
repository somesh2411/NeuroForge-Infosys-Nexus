package com.neuroforge.nexus.projects.dto;

import java.time.LocalDateTime;

public record MilestoneResponse(
    String id,
    String projectId,
    String name,
    String description,
    LocalDateTime targetDate,
    String status,
    LocalDateTime createdAt
) {}
