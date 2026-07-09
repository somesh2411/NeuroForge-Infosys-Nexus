package com.neuroforge.nexus.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserSyncRequest(
    @NotBlank String id,
    @NotBlank String username,
    @NotBlank @Email String email,
    String firstName,
    String lastName
) {}
