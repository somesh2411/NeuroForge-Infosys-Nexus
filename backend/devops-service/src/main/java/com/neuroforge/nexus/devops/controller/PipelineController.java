package com.neuroforge.nexus.devops.controller;

import com.neuroforge.nexus.devops.dto.PipelineRequest;
import com.neuroforge.nexus.devops.dto.PipelineResponse;
import com.neuroforge.nexus.devops.service.PipelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pipelines")
@Tag(name = "Pipeline Management", description = "Endpoints for creating and tracking DevOps pipelines")
public class PipelineController {

    private static final Logger log = LoggerFactory.getLogger(PipelineController.class);
    private final PipelineService pipelineService;

    public PipelineController(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get all pipelines for a specific project")
    public ResponseEntity<List<PipelineResponse>> getPipelinesByProject(@PathVariable("projectId") String projectId) {
        log.info("REST request to get pipelines for project: {}", projectId);
        return ResponseEntity.ok(pipelineService.getPipelinesByProject(projectId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get pipeline details by ID")
    public ResponseEntity<PipelineResponse> getPipelineById(@PathVariable("id") String id) {
        log.info("REST request to get pipeline: {}", id);
        return ResponseEntity.ok(pipelineService.getPipelineById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Create a new pipeline (supports templates)")
    public ResponseEntity<PipelineResponse> createPipeline(@Valid @RequestBody PipelineRequest request) {
        log.info("REST request to create pipeline: {}", request.name());
        return new ResponseEntity<>(pipelineService.createPipeline(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Update an existing pipeline configuration")
    public ResponseEntity<PipelineResponse> updatePipeline(
            @PathVariable("id") String id,
            @Valid @RequestBody PipelineRequest request) {
        log.info("REST request to update pipeline: {}", id);
        return ResponseEntity.ok(pipelineService.updatePipeline(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Soft delete a pipeline configuration")
    public ResponseEntity<Void> deletePipeline(@PathVariable("id") String id) {
        log.info("REST request to delete pipeline: {}", id);
        pipelineService.deletePipeline(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Enable or disable a pipeline run execution")
    public ResponseEntity<PipelineResponse> togglePipeline(
            @PathVariable("id") String id,
            @RequestParam("enabled") boolean enabled) {
        log.info("REST request to toggle pipeline {} state to {}", id, enabled);
        return ResponseEntity.ok(pipelineService.togglePipeline(id, enabled));
    }
}
