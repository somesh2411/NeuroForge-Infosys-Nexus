package com.neuroforge.nexus.sprints.controller;

import com.neuroforge.nexus.sprints.dto.BlockerRequest;
import com.neuroforge.nexus.sprints.dto.BlockerResponse;
import com.neuroforge.nexus.sprints.service.BlockerService;
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
@RequestMapping("/api/v1/blockers")
@RequiredArgsConstructor
@Tag(name = "Blocker Management", description = "Endpoints for logging, tracking, and resolving blockers on tasks")
public class BlockerController {

    private final BlockerService blockerService;

    @PostMapping("/task/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD', 'DEVELOPER')")
    @Operation(summary = "Log a blocker on a task")
    public ResponseEntity<BlockerResponse> addBlocker(
            @PathVariable("taskId") String taskId,
            @Valid @RequestBody BlockerRequest request) {
        log.info("REST request to add blocker to task {}", taskId);
        return new ResponseEntity<>(blockerService.addBlocker(taskId, request), HttpStatus.CREATED);
    }

    @PutMapping("/{blockerId}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD', 'DEVELOPER')")
    @Operation(summary = "Mark a blocker as resolved")
    public ResponseEntity<BlockerResponse> resolveBlocker(@PathVariable("blockerId") String blockerId) {
        log.info("REST request to resolve blocker {}", blockerId);
        return ResponseEntity.ok(blockerService.resolveBlocker(blockerId));
    }

    @DeleteMapping("/{blockerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Remove a blocker from a task")
    public ResponseEntity<Void> deleteBlocker(@PathVariable("blockerId") String blockerId) {
        log.info("REST request to delete blocker: {}", blockerId);
        blockerService.deleteBlocker(blockerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/task/{taskId}")
    @Operation(summary = "Get all blockers for a task")
    public ResponseEntity<List<BlockerResponse>> getBlockersByTask(@PathVariable("taskId") String taskId) {
        log.info("REST request to get blockers for task: {}", taskId);
        return ResponseEntity.ok(blockerService.getBlockersByTask(taskId));
    }
}
