package com.neuroforge.nexus.users.event;

public record TeamCreatedEvent(
    String id,
    String name,
    String code,
    String description,
    String leadId
) {}
