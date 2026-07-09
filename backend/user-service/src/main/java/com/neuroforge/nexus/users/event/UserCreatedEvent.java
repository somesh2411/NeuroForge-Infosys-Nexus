package com.neuroforge.nexus.users.event;

public record UserCreatedEvent(
    String id,
    String username,
    String email,
    String firstName,
    String lastName
) {}
