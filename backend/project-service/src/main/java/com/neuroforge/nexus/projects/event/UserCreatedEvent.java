package com.neuroforge.nexus.projects.event;

public record UserCreatedEvent(
    String id,
    String username,
    String email,
    String firstName,
    String lastName
) {}
