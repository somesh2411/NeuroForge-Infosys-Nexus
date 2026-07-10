package com.neuroforge.nexus.devops.controller;

import com.neuroforge.nexus.devops.dto.EnvironmentRequest;
import com.neuroforge.nexus.devops.dto.EnvironmentResponse;
import com.neuroforge.nexus.devops.service.EnvironmentService;
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
@RequestMapping("/api/v1/devops-environments")
@Tag(name = "Environment Management", description = "Endpoints for configuring and checking status of deployment environments")
public class EnvironmentController {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentController.class);
    private final EnvironmentService environmentService;

    public EnvironmentController(EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    @GetMapping
    @Operation(summary = "Get all deployment environments")
    public ResponseEntity<List<EnvironmentResponse>> getAllEnvironments() {
        log.info("REST request to get all environments");
        return ResponseEntity.ok(environmentService.getAllEnvironments());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get environment details by ID")
    public ResponseEntity<EnvironmentResponse> getEnvironmentById(@PathVariable("id") String id) {
        log.info("REST request to get environment: {}", id);
        return ResponseEntity.ok(environmentService.getEnvironmentById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER')")
    @Operation(summary = "Create a new deployment environment")
    public ResponseEntity<EnvironmentResponse> createEnvironment(@Valid @RequestBody EnvironmentRequest request) {
        log.info("REST request to create environment: {}", request.name());
        return new ResponseEntity<>(environmentService.createEnvironment(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER')")
    @Operation(summary = "Update an environment's metadata")
    public ResponseEntity<EnvironmentResponse> updateEnvironment(
            @PathVariable("id") String id,
            @Valid @RequestBody EnvironmentRequest request) {
        log.info("REST request to update environment: {}", id);
        return ResponseEntity.ok(environmentService.updateEnvironment(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER')")
    @Operation(summary = "Delete an environment definition")
    public ResponseEntity<Void> deleteEnvironment(@PathVariable("id") String id) {
        log.info("REST request to delete environment: {}", id);
        environmentService.deleteEnvironment(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER')")
    @Operation(summary = "Enable or disable a deployment environment")
    public ResponseEntity<EnvironmentResponse> toggleEnvironment(
            @PathVariable("id") String id,
            @RequestParam("enabled") boolean enabled) {
        log.info("REST request to toggle environment {} enabled status to {}", id, enabled);
        return ResponseEntity.ok(environmentService.toggleEnvironmentStatus(id, enabled));
    }
}
