package com.neuroforge.nexus.devops.controller;

import com.neuroforge.nexus.devops.dto.BuildCompareResponse;
import com.neuroforge.nexus.devops.dto.BuildResponse;
import com.neuroforge.nexus.devops.dto.PipelineStageResponse;
import com.neuroforge.nexus.devops.service.BuildService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/builds")
@Tag(name = "Build Execution Tracker", description = "Endpoints for triggering pipeline builds, fetching stage progressions, logs, and comparing run builds")
public class BuildController {

    private static final Logger log = LoggerFactory.getLogger(BuildController.class);
    private final BuildService buildService;

    public BuildController(BuildService buildService) {
        this.buildService = buildService;
    }

    @GetMapping("/pipeline/{pipelineId}")
    @Operation(summary = "Get execution history for a pipeline")
    public ResponseEntity<List<BuildResponse>> getBuildsByPipeline(@PathVariable("pipelineId") String pipelineId) {
        log.info("REST request to get builds for pipeline: {}", pipelineId);
        return ResponseEntity.ok(buildService.getBuildsByPipeline(pipelineId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get build run details by ID")
    public ResponseEntity<BuildResponse> getBuildById(@PathVariable("id") String id) {
        log.info("REST request to get build: {}", id);
        return ResponseEntity.ok(buildService.getBuildById(id));
    }

    @PostMapping("/pipeline/{pipelineId}/trigger")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Trigger a new pipeline execution run (supports Jenkins & GitHub)")
    public ResponseEntity<BuildResponse> triggerBuild(
            @PathVariable("pipelineId") String pipelineId,
            @RequestParam(value = "triggerType", defaultValue = "MANUAL") String triggerType) {
        log.info("REST request to trigger pipeline: {}", pipelineId);
        return new ResponseEntity<>(buildService.triggerBuild(pipelineId, triggerType), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/stages")
    @Operation(summary = "Get status and logs of all stages for a build run")
    public ResponseEntity<List<PipelineStageResponse>> getBuildStages(@PathVariable("id") String id) {
        log.info("REST request to get stages for build: {}", id);
        return ResponseEntity.ok(buildService.getBuildStages(id));
    }

    @GetMapping("/compare")
    @Operation(summary = "Compare statistics between two execution runs")
    public ResponseEntity<BuildCompareResponse> compareBuilds(
            @RequestParam("buildIdA") String buildIdA,
            @RequestParam("buildIdB") String buildIdB) {
        log.info("REST request to compare build {} with {}", buildIdA, buildIdB);
        return ResponseEntity.ok(buildService.compareBuilds(buildIdA, buildIdB));
    }

    @GetMapping("/project/{projectId}/recent")
    @Operation(summary = "Get top 10 recent builds for a project")
    public ResponseEntity<List<BuildResponse>> getRecentProjectBuilds(@PathVariable("projectId") String projectId) {
        log.info("REST request to get recent builds for project: {}", projectId);
        return ResponseEntity.ok(buildService.getRecentProjectBuilds(projectId));
    }
}
