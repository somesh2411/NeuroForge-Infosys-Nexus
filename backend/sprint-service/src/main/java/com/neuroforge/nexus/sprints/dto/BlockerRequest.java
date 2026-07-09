package com.neuroforge.nexus.sprints.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BlockerRequest(
    @NotBlank(message = "Blocker details are required")
    @Size(max = 200, message = "Blocker details cannot exceed 200 characters")
    String name
) {}
