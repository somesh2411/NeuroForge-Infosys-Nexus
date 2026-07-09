package com.neuroforge.nexus.projects.controller;

import com.neuroforge.nexus.projects.dto.ProjectRequest;
import com.neuroforge.nexus.projects.dto.ProjectResponse;
import com.neuroforge.nexus.projects.service.ProjectService;
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
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Project Management", description = "Endpoints for project setup, updates, team alignments, and archival")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Create a new project")
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request) {
        log.info("REST request to create project: {}", request.name());
        return new ResponseEntity<>(projectService.createProject(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Update an existing project (Admins, Owners, and Leads only)")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable("id") String id,
            @Valid @RequestBody ProjectRequest request) {
        log.info("REST request to update project: {}", id);
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project details by identifier")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable("id") String id) {
        log.info("REST request to get project by id: {}", id);
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @GetMapping("/key/{key}")
    @Operation(summary = "Get project details by project Key")
    public ResponseEntity<ProjectResponse> getProjectByKey(@PathVariable("key") String key) {
        log.info("REST request to get project by key: {}", key);
        return ResponseEntity.ok(projectService.getProjectByKey(key));
    }

    @GetMapping
    @Operation(summary = "Retrieve a list of all active projects")
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        log.info("REST request to get all projects");
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER')")
    @Operation(summary = "Soft delete / archive a project (Admins and Owners only)")
    public ResponseEntity<Void> deleteProject(@PathVariable("id") String id) {
        log.info("REST request to delete project: {}", id);
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/teams")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Associate a team with a project")
    public ResponseEntity<ProjectResponse> associateTeam(
            @PathVariable("id") String id,
            @RequestParam("teamId") String teamId) {
        log.info("REST request to associate team {} with project {}", teamId, id);
        return ResponseEntity.ok(projectService.associateTeam(id, teamId));
    }

    @DeleteMapping("/{id}/teams/{teamId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Remove a team association from a project")
    public ResponseEntity<ProjectResponse> disassociateTeam(
            @PathVariable("id") String id,
            @PathVariable("teamId") String teamId) {
        log.info("REST request to disassociate team {} from project {}", teamId, id);
        return ResponseEntity.ok(projectService.disassociateTeam(id, teamId));
    }
}
