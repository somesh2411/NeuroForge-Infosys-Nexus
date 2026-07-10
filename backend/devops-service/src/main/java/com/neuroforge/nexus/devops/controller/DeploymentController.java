package com.neuroforge.nexus.devops.controller;

import com.neuroforge.nexus.devops.dto.DeploymentRequest;
import com.neuroforge.nexus.devops.dto.DeploymentResponse;
import com.neuroforge.nexus.devops.service.DeploymentService;
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
@RequestMapping("/api/v1/deployments")
@Tag(name = "Deployment Registry", description = "Endpoints for triggering new deployments, rollbacks, and viewing environment history")
public class DeploymentController {

    private static final Logger log = LoggerFactory.getLogger(DeploymentController.class);
    private final DeploymentService deploymentService;

    public DeploymentController(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    @GetMapping("/environment/{environmentId}")
    @Operation(summary = "Get deployment logs for a specific environment")
    public ResponseEntity<List<DeploymentResponse>> getDeploymentsByEnvironment(@PathVariable("environmentId") String environmentId) {
        log.info("REST request to get deployments for environment: {}", environmentId);
        return ResponseEntity.ok(deploymentService.getDeploymentsByEnvironment(environmentId));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get all deployments history for a project")
    public ResponseEntity<List<DeploymentResponse>> getProjectDeployments(@PathVariable("projectId") String projectId) {
        log.info("REST request to get deployments for project: {}", projectId);
        return ResponseEntity.ok(deploymentService.getProjectDeployments(projectId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Deploy a successful build to an environment")
    public ResponseEntity<DeploymentResponse> triggerDeployment(@Valid @RequestBody DeploymentRequest request) {
        log.info("REST request to trigger deployment version: {}", request.version());
        return new ResponseEntity<>(deploymentService.triggerDeployment(request), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/rollback")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Execute emergency rollback to previous successful deployment version")
    public ResponseEntity<DeploymentResponse> rollbackDeployment(
            @PathVariable("id") String id,
            @RequestParam(value = "reason", required = false) String reason) {
        log.info("REST request to roll back deployment: {}", id);
        return ResponseEntity.ok(deploymentService.rollbackDeployment(id, reason));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get deployment details by ID")
    public ResponseEntity<DeploymentResponse> getDeploymentById(@PathVariable("id") String id) {
        log.info("REST request to get deployment: {}", id);
        return ResponseEntity.ok(deploymentService.getDeploymentById(id));
    }
}
