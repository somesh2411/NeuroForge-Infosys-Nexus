package com.neuroforge.nexus.sprints.controller;

import com.neuroforge.nexus.sprints.dto.CommentRequest;
import com.neuroforge.nexus.sprints.dto.CommentResponse;
import com.neuroforge.nexus.sprints.service.CommentService;
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
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Tag(name = "Comment Management", description = "Endpoints for discussing, commenting, and posting developer notes on tasks")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/task/{taskId}")
    @Operation(summary = "Add a comment/discussion to a task")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable("taskId") String taskId,
            @Valid @RequestBody CommentRequest request) {
        log.info("REST request to add comment to task {}", taskId);
        return new ResponseEntity<>(commentService.addComment(taskId, request), HttpStatus.CREATED);
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Delete a task comment")
    public ResponseEntity<Void> deleteComment(@PathVariable("commentId") String commentId) {
        log.info("REST request to delete comment: {}", commentId);
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/task/{taskId}")
    @Operation(summary = "Get comments thread for a task")
    public ResponseEntity<List<CommentResponse>> getCommentsByTask(@PathVariable("taskId") String taskId) {
        log.info("REST request to get comments for task: {}", taskId);
        return ResponseEntity.ok(commentService.getCommentsByTask(taskId));
    }
}
