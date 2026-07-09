package com.neuroforge.nexus.users.dto;

import java.time.LocalDateTime;

public record UserResponse(
    String id,
    String username,
    String email,
    String firstName,
    String lastName,
    String primaryTeamId,
    String primaryTeamName,
    String role,
    LocalDateTime createdAt
) {}
