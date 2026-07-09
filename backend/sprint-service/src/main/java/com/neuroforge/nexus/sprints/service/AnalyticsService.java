package com.neuroforge.nexus.sprints.service;

import com.neuroforge.nexus.sprints.dto.ActivityLogResponse;
import com.neuroforge.nexus.sprints.dto.BurndownPoint;
import com.neuroforge.nexus.sprints.dto.SprintMetricsResponse;
import com.neuroforge.nexus.sprints.dto.VelocityPoint;

import java.util.List;

public interface AnalyticsService {
    SprintMetricsResponse getSprintMetrics(String sprintId);
    List<BurndownPoint> getBurndownData(String sprintId);
    List<VelocityPoint> getProjectVelocity(String projectId);
    List<ActivityLogResponse> getTaskActivity(String taskId);
    List<ActivityLogResponse> getSprintActivity(String sprintId);
}
