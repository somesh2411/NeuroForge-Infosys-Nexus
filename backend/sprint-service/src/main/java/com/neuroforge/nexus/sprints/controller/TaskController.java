package com.neuroforge.nexus.sprints.controller;

import com.neuroforge.nexus.sprints.dto.TaskRequest;
import com.neuroforge.nexus.sprints.dto.TaskResponse;
import com.neuroforge.nexus.sprints.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Management", description = "Endpoints for managing agile tasks, backlog, priorities, status workflow, and assignee")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Create a new task (backlog or sprint)")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        log.info("REST request to create task: {}", request.title());
        return new ResponseEntity<>(taskService.createTask(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Update an existing task details")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable("id") String id,
            @Valid @RequestBody TaskRequest request) {
        log.info("REST request to update task: {}", id);
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task details by ID")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable("id") String id) {
        log.info("REST request to get task: {}", id);
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @GetMapping
    @Operation(summary = "Retrieve all tasks with advanced pagination, sorting, search, and filtering")
    public ResponseEntity<Page<TaskResponse>> getTasks(
            @RequestParam(value = "projectId", required = false) String projectId,
            @RequestParam(value = "sprintId", required = false) String sprintId,
            @RequestParam(value = "developerId", required = false) String developerId,
            @RequestParam(value = "priority", required = false) String priority,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "DESC") String sortDir,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("REST request to get filtered tasks");
        return ResponseEntity.ok(taskService.getTasks(projectId, sprintId, developerId, priority, status, search, sortBy, sortDir, page, size));
    }

    @GetMapping("/sprint/{sprintId}")
    @Operation(summary = "Get all tasks for a specific sprint")
    public ResponseEntity<List<TaskResponse>> getTasksBySprint(@PathVariable("sprintId") String sprintId) {
        log.info("REST request to get tasks for sprint: {}", sprintId);
        return ResponseEntity.ok(taskService.getTasksBySprint(sprintId));
    }

    @GetMapping("/project/{projectId}/backlog")
    @Operation(summary = "Get all backlog tasks for a project")
    public ResponseEntity<List<TaskResponse>> getBacklogTasks(@PathVariable("projectId") String projectId) {
        log.info("REST request to get backlog tasks for project: {}", projectId);
        return ResponseEntity.ok(taskService.getBacklogTasks(projectId));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD', 'DEVELOPER')")
    @Operation(summary = "Update task status with optimistic locking check (useful for Kanban drag-and-drop)")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable("id") String id,
            @RequestParam("status") String status,
            @RequestParam("version") int version) {
        log.info("REST request to drag task {} to status {}", id, status);
        return ResponseEntity.ok(taskService.updateTaskStatus(id, status, version));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Soft delete a task")
    public ResponseEntity<Void> deleteTask(@PathVariable("id") String id) {
        log.info("REST request to delete task: {}", id);
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
