package com.neuroforge.nexus.devops.dto;

import java.time.LocalDateTime;

public record EnvironmentResponse(
    String id,
    String name,
    String description,
    boolean enabled,
    LocalDateTime createdAt,
    String createdBy
) {}
