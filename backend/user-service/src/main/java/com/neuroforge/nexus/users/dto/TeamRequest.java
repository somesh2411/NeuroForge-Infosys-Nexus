package com.neuroforge.nexus.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TeamRequest(
    @NotBlank String name,
    @NotBlank @Pattern(regexp = "^[a-zA-Z0-9_-]{3,10}$", message = "Code must be 3-10 alphanumeric characters") String code,
    String description,
    String leadId
) {}
