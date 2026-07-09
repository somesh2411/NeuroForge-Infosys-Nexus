package com.neuroforge.nexus.projects.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectResponse(
    String id,
    String name,
    String key,
    String description,
    String managerId,
    String managerName,
    String status,
    List<String> teamIds,
    LocalDateTime createdAt
) {}
