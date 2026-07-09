package com.neuroforge.nexus.users.event;

public record UserUpdatedEvent(
    String id,
    String username,
    String email,
    String firstName,
    String lastName
) {}
