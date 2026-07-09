package com.neuroforge.nexus.sprints.event;

public record UserUpdatedEvent(
    String id,
    String username,
    String email,
    String firstName,
    String lastName
) {}
