package com.neuroforge.nexus.projects.event;

public record ProjectCreatedEvent(
    String id,
    String name,
    String key
) {}
