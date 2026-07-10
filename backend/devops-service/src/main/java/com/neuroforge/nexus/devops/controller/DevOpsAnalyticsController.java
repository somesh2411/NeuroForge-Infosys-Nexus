package com.neuroforge.nexus.devops.controller;

import com.neuroforge.nexus.devops.dto.DevOpsMetricsResponse;
import com.neuroforge.nexus.devops.service.DevOpsAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/devops-analytics")
@Tag(name = "DevOps Analytics", description = "Endpoints for dynamic build success, deployment duration, and pipeline performance KPI calculations")
public class DevOpsAnalyticsController {

    private static final Logger log = LoggerFactory.getLogger(DevOpsAnalyticsController.class);
    private final DevOpsAnalyticsService analyticsService;

    public DevOpsAnalyticsController(DevOpsAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get build success ratios and deployment statistics for a project")
    public ResponseEntity<DevOpsMetricsResponse> getProjectDevOpsMetrics(@PathVariable("projectId") String projectId) {
        log.info("REST request to get DevOps metrics for project: {}", projectId);
        return ResponseEntity.ok(analyticsService.getProjectDevOpsMetrics(projectId));
    }
}
