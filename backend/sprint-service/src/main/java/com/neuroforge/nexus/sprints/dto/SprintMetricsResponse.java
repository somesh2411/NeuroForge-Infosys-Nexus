package com.neuroforge.nexus.sprints.dto;

public record SprintMetricsResponse(
    String sprintId,
    String sprintName,
    String sprintGoal,
    String status,
    long totalTasks,
    long completedTasks,
    long remainingTasks,
    int totalStoryPoints,
    int completedStoryPoints,
    int remainingStoryPoints,
    double progressPercentage
) {}
