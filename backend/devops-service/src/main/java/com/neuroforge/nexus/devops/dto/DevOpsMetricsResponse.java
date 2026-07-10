package com.neuroforge.nexus.devops.dto;

import java.util.List;
import java.util.Map;

public record DevOpsMetricsResponse(
    long totalBuilds,
    long successfulBuilds,
    long failedBuilds,
    long runningBuilds,
    double buildSuccessRate,
    double averageBuildDurationSec,
    long totalDeployments,
    long successfulDeployments,
    double deploymentSuccessRate,
    long totalReleases,
    List<Map<String, Object>> buildTrends, // e.g. [{date: "07/10", success: 5, failed: 1}]
    List<Map<String, Object>> envStatus // e.g. [{env: "Production", activeVersion: "v1.2.0", lastDeployStatus: "SUCCESSFUL"}]
) {}
