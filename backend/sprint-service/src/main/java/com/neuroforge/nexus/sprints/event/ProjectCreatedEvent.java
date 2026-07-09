package com.neuroforge.nexus.sprints.event;

public record ProjectCreatedEvent(
    String id,
    String name,
    String key
) {}
