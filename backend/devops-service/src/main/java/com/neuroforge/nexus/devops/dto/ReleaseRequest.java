package com.neuroforge.nexus.devops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ReleaseRequest(
    @NotBlank(message = "Version is required")
    @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9.]+)?$", message = "Version must follow semantic versioning (e.g., 1.0.0 or 1.2.3-beta)")
    String version,

    @NotBlank(message = "Release name is required")
    String name,

    String releaseNotes,

    @NotBlank(message = "Build ID is required")
    String buildId,

    @NotBlank(message = "Release status is required")
    String status // DRAFT, TESTING, APPROVED, RELEASED, ARCHIVED
) {}
