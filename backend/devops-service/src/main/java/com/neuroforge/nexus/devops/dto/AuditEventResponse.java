package com.neuroforge.nexus.devops.dto;

import java.time.LocalDateTime;

public record AuditEventResponse(
    String id,
    String eventType,
    String message,
    String actor,
    LocalDateTime timestamp
) {}
