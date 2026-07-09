package com.neuroforge.nexus.projects.event;

public record TeamCreatedEvent(
    String id,
    String name,
    String code,
    String description,
    String leadId
) {}
