package com.neuroforge.nexus.devops.controller;

import com.neuroforge.nexus.devops.dto.ReleaseRequest;
import com.neuroforge.nexus.devops.dto.ReleaseResponse;
import com.neuroforge.nexus.devops.service.ReleaseService;
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
@RequestMapping("/api/v1/releases")
@Tag(name = "Release Management", description = "Endpoints for managing software releases and version details")
public class ReleaseController {

    private static final Logger log = LoggerFactory.getLogger(ReleaseController.class);
    private final ReleaseService releaseService;

    public ReleaseController(ReleaseService releaseService) {
        this.releaseService = releaseService;
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get all releases for a specific project")
    public ResponseEntity<List<ReleaseResponse>> getReleasesByProject(@PathVariable("projectId") String projectId) {
        log.info("REST request to get releases for project: {}", projectId);
        return ResponseEntity.ok(releaseService.getReleasesByProject(projectId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get release details by ID")
    public ResponseEntity<ReleaseResponse> getReleaseById(@PathVariable("id") String id) {
        log.info("REST request to get release: {}", id);
        return ResponseEntity.ok(releaseService.getReleaseById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Draft a new release version linked to a successful build")
    public ResponseEntity<ReleaseResponse> createRelease(@Valid @RequestBody ReleaseRequest request) {
        log.info("REST request to create release: {}", request.version());
        return new ResponseEntity<>(releaseService.createRelease(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Update an existing release parameters or publish status")
    public ResponseEntity<ReleaseResponse> updateRelease(
            @PathVariable("id") String id,
            @Valid @RequestBody ReleaseRequest request) {
        log.info("REST request to update release: {}", id);
        return ResponseEntity.ok(releaseService.updateRelease(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Soft delete a release definition")
    public ResponseEntity<Void> deleteRelease(@PathVariable("id") String id) {
        log.info("REST request to delete release: {}", id);
        releaseService.deleteRelease(id);
        return ResponseEntity.noContent().build();
    }
}
