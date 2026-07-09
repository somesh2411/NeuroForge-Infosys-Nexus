package com.neuroforge.nexus.sprints.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

public record SprintRequest(
    @NotBlank String name,
    String goal,
    LocalDateTime startDate,
    LocalDateTime endDate,
    @PositiveOrZero int capacity,
    String status
) {}
