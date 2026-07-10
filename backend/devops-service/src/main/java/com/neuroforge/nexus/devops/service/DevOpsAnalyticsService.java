package com.neuroforge.nexus.devops.service;

import com.neuroforge.nexus.devops.dto.DevOpsMetricsResponse;

public interface DevOpsAnalyticsService {
    DevOpsMetricsResponse getProjectDevOpsMetrics(String projectId);
}
