package com.neuroforge.nexus.sprints.dto;

public record VelocityPoint(
    String sprintId,
    String sprintName,
    int completedStoryPoints
) {}
