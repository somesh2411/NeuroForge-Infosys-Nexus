package com.neuroforge.nexus.users.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TeamResponse(
    String id,
    String name,
    String code,
    String description,
    String leadId,
    String leadName,
    List<String> memberIds,
    LocalDateTime createdAt
) {}
