package com.neuroforge.nexus.devops.dto;

import jakarta.validation.constraints.NotBlank;

public record DeploymentRequest(
    @NotBlank(message = "Environment ID is required")
    String environmentId,

    @NotBlank(message = "Build ID is required")
    String buildId,

    @NotBlank(message = "Version is required")
    String version
) {}
