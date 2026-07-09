package com.neuroforge.nexus.sprints.controller;

import com.neuroforge.nexus.sprints.dto.SprintRequest;
import com.neuroforge.nexus.sprints.dto.SprintResponse;
import com.neuroforge.nexus.sprints.service.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/sprints")
@RequiredArgsConstructor
@Tag(name = "Sprint Management", description = "Endpoints for scheduling sprints, capacity configuration, and goals")
public class SprintController {

    private final SprintService sprintService;

    @PostMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Create a new sprint for a project")
    public ResponseEntity<SprintResponse> createSprint(
            @PathVariable("projectId") String projectId,
            @Valid @RequestBody SprintRequest request) {
        log.info("REST request to create sprint for project: {}", projectId);
        return new ResponseEntity<>(sprintService.createSprint(projectId, request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Update an existing sprint's settings")
    public ResponseEntity<SprintResponse> updateSprint(
            @PathVariable("id") String id,
            @Valid @RequestBody SprintRequest request) {
        log.info("REST request to update sprint: {}", id);
        return ResponseEntity.ok(sprintService.updateSprint(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sprint details by ID")
    public ResponseEntity<SprintResponse> getSprintById(@PathVariable("id") String id) {
        log.info("REST request to get sprint by id: {}", id);
        return ResponseEntity.ok(sprintService.getSprintById(id));
    }

    @GetMapping
    @Operation(summary = "Retrieve all active sprints")
    public ResponseEntity<List<SprintResponse>> getAllSprints() {
        log.info("REST request to get all sprints");
        return ResponseEntity.ok(sprintService.getAllSprints());
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Retrieve all sprints associated with a project")
    public ResponseEntity<List<SprintResponse>> getSprintsByProject(@PathVariable("projectId") String projectId) {
        log.info("REST request to get sprints for project: {}", projectId);
        return ResponseEntity.ok(sprintService.getSprintsByProject(projectId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Soft delete a sprint")
    public ResponseEntity<Void> deleteSprint(@PathVariable("id") String id) {
        log.info("REST request to delete sprint: {}", id);
        sprintService.deleteSprint(id);
        return ResponseEntity.noContent().build();
    }
}
