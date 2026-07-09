package com.neuroforge.nexus.sprints.event;

public record UserCreatedEvent(
    String id,
    String username,
    String email,
    String firstName,
    String lastName
) {}
