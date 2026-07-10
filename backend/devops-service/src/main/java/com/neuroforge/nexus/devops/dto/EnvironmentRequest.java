package com.neuroforge.nexus.devops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnvironmentRequest(
    @NotBlank(message = "Environment name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    String name,

    String description,
    
    boolean enabled
) {}
