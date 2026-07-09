package com.neuroforge.nexus.projects.event;

public record UserUpdatedEvent(
    String id,
    String username,
    String email,
    String firstName,
    String lastName
) {}
