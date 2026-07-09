package com.neuroforge.nexus.sprints.controller;

import com.neuroforge.nexus.sprints.dto.ActivityLogResponse;
import com.neuroforge.nexus.sprints.dto.BurndownPoint;
import com.neuroforge.nexus.sprints.dto.SprintMetricsResponse;
import com.neuroforge.nexus.sprints.dto.VelocityPoint;
import com.neuroforge.nexus.sprints.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Sprint Analytics", description = "Endpoints for dynamic sprint metrics, velocity tracking, and burndown reports")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/sprint/{sprintId}/metrics")
    @Operation(summary = "Retrieve real-time sprint statistics (Goal, task counts, story point ratios)")
    public ResponseEntity<SprintMetricsResponse> getSprintMetrics(@PathVariable("sprintId") String sprintId) {
        log.info("REST request to get sprint metrics for sprint {}", sprintId);
        return ResponseEntity.ok(analyticsService.getSprintMetrics(sprintId));
    }

    @GetMapping("/sprint/{sprintId}/burndown")
    @Operation(summary = "Retrieve daily burndown coordinates (Ideal vs Actual story points remaining)")
    public ResponseEntity<List<BurndownPoint>> getBurndownData(@PathVariable("sprintId") String sprintId) {
        log.info("REST request to get burndown chart for sprint {}", sprintId);
        return ResponseEntity.ok(analyticsService.getBurndownData(sprintId));
    }

    @GetMapping("/project/{projectId}/velocity")
    @Operation(summary = "Retrieve project-wide sprint velocity measurements (Historical points completed)")
    public ResponseEntity<List<VelocityPoint>> getProjectVelocity(@PathVariable("projectId") String projectId) {
        log.info("REST request to get velocity charts for project {}", projectId);
        return ResponseEntity.ok(analyticsService.getProjectVelocity(projectId));
    }

    @GetMapping("/task/{taskId}/activity")
    @Operation(summary = "Retrieve audit trail of changes for a specific task")
    public ResponseEntity<List<ActivityLogResponse>> getTaskActivity(@PathVariable("taskId") String taskId) {
        log.info("REST request to get task activity logs for task {}", taskId);
        return ResponseEntity.ok(analyticsService.getTaskActivity(taskId));
    }

    @GetMapping("/sprint/{sprintId}/activity")
    @Operation(summary = "Retrieve timeline of events for an entire sprint")
    public ResponseEntity<List<ActivityLogResponse>> getSprintActivity(@PathVariable("sprintId") String sprintId) {
        log.info("REST request to get sprint activity logs for sprint {}", sprintId);
        return ResponseEntity.ok(analyticsService.getSprintActivity(sprintId));
    }
}
