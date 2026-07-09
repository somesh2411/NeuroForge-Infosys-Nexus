package com.neuroforge.nexus.projects.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record MilestoneRequest(
    @NotBlank String name,
    String description,
    LocalDateTime targetDate,
    String status
) {}
