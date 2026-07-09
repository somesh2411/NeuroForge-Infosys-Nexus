package com.neuroforge.nexus.projects.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ProjectRequest(
    @NotBlank String name,
    @NotBlank @Pattern(regexp = "^[a-zA-Z]{2,10}$", message = "Key must be 2-10 letters") String key,
    String description,
    @NotBlank String managerId,
    String status
) {}
