package com.neuroforge.nexus.projects.controller;

import com.neuroforge.nexus.projects.dto.MilestoneRequest;
import com.neuroforge.nexus.projects.dto.MilestoneResponse;
import com.neuroforge.nexus.projects.service.MilestoneService;
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
@RequestMapping("/api/v1/milestones")
@RequiredArgsConstructor
@Tag(name = "Milestone Management", description = "Endpoints for project milestones, goals, and target deadlines")
public class MilestoneController {

    private final MilestoneService milestoneService;

    @PostMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Create a new milestone for a project")
    public ResponseEntity<MilestoneResponse> createMilestone(
            @PathVariable("projectId") String projectId,
            @Valid @RequestBody MilestoneRequest request) {
        log.info("REST request to create milestone for project: {}", projectId);
        return new ResponseEntity<>(milestoneService.createMilestone(projectId, request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Update an existing milestone")
    public ResponseEntity<MilestoneResponse> updateMilestone(
            @PathVariable("id") String id,
            @Valid @RequestBody MilestoneRequest request) {
        log.info("REST request to update milestone: {}", id);
        return ResponseEntity.ok(milestoneService.updateMilestone(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get milestone details by ID")
    public ResponseEntity<MilestoneResponse> getMilestoneById(@PathVariable("id") String id) {
        log.info("REST request to get milestone by id: {}", id);
        return ResponseEntity.ok(milestoneService.getMilestoneById(id));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Retrieve all milestones associated with a project")
    public ResponseEntity<List<MilestoneResponse>> getMilestonesByProject(@PathVariable("projectId") String projectId) {
        log.info("REST request to get milestones for project: {}", projectId);
        return ResponseEntity.ok(milestoneService.getMilestonesByProject(projectId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Soft delete a milestone")
    public ResponseEntity<Void> deleteMilestone(@PathVariable("id") String id) {
        log.info("REST request to delete milestone: {}", id);
        milestoneService.deleteMilestone(id);
        return ResponseEntity.noContent().build();
    }
}
