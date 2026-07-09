package com.neuroforge.nexus.sprints.service.impl;

import com.neuroforge.nexus.shared.exception.ResourceNotFoundException;
import com.neuroforge.nexus.shared.util.SecurityUtils;
import com.neuroforge.nexus.sprints.domain.ActivityLog;
import com.neuroforge.nexus.sprints.domain.Task;
import com.neuroforge.nexus.sprints.domain.TaskComment;
import com.neuroforge.nexus.sprints.dto.CommentRequest;
import com.neuroforge.nexus.sprints.dto.CommentResponse;
import com.neuroforge.nexus.sprints.repository.ActivityLogRepository;
import com.neuroforge.nexus.sprints.repository.TaskCommentRepository;
import com.neuroforge.nexus.sprints.repository.TaskRepository;
import com.neuroforge.nexus.sprints.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final TaskCommentRepository taskCommentRepository;
    private final TaskRepository taskRepository;
    private final ActivityLogRepository activityLogRepository;

    @Override
    @Transactional
    public CommentResponse addComment(String taskId, CommentRequest request) {
        log.info("Adding comment to task {}: {}", taskId, request.content());
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        String author = SecurityUtils.getCurrentUsername();

        TaskComment comment = new TaskComment();
        comment.setId(UUID.randomUUID().toString());
        comment.setTask(task);
        comment.setContent(request.content());
        comment.setAuthorUsername(author);

        TaskComment saved = taskCommentRepository.save(comment);

        // Activity Logging
        logActivity(task.getId(), task.getSprint() != null ? task.getSprint().getId() : null,
                "COMMENT_ADDED", author + " added a comment: '" + truncateContent(request.content()) + "'");

        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteComment(String commentId) {
        log.info("Deleting comment: {}", commentId);
        TaskComment comment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        comment.softDelete(SecurityUtils.getCurrentUserId());
        taskCommentRepository.save(comment);

        Task task = comment.getTask();

        // Activity Logging
        logActivity(task.getId(), task.getSprint() != null ? task.getSprint().getId() : null,
                "COMMENT_REMOVED", "Comment by " + comment.getAuthorUsername() + " was deleted.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTask(String taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found with id: " + taskId);
        }
        return taskCommentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private void logActivity(String taskId, String sprintId, String eventType, String message) {
        ActivityLog logEntry = new ActivityLog();
        logEntry.setId(UUID.randomUUID().toString());
        logEntry.setTaskId(taskId);
        logEntry.setSprintId(sprintId);
        logEntry.setEventType(eventType);
        logEntry.setMessage(message);
        logEntry.setActor(SecurityUtils.getCurrentUserId());
        activityLogRepository.save(logEntry);
    }

    private String truncateContent(String content) {
        if (content == null) return "";
        return content.length() > 60 ? content.substring(0, 57) + "..." : content;
    }

    private CommentResponse toResponse(TaskComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getTask().getId(),
                comment.getContent(),
                comment.getAuthorUsername(),
                comment.getCreatedAt()
        );
    }
}
