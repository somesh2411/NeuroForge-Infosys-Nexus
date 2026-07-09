package com.neuroforge.nexus.sprints.service.impl;

import com.neuroforge.nexus.shared.exception.ResourceNotFoundException;
import com.neuroforge.nexus.shared.util.SecurityUtils;
import com.neuroforge.nexus.sprints.domain.ActivityLog;
import com.neuroforge.nexus.sprints.domain.Blocker;
import com.neuroforge.nexus.sprints.domain.Task;
import com.neuroforge.nexus.sprints.dto.BlockerRequest;
import com.neuroforge.nexus.sprints.dto.BlockerResponse;
import com.neuroforge.nexus.sprints.repository.ActivityLogRepository;
import com.neuroforge.nexus.sprints.repository.BlockerRepository;
import com.neuroforge.nexus.sprints.repository.TaskRepository;
import com.neuroforge.nexus.sprints.service.BlockerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockerServiceImpl implements BlockerService {

    private final BlockerRepository blockerRepository;
    private final TaskRepository taskRepository;
    private final ActivityLogRepository activityLogRepository;

    @Override
    @Transactional
    public BlockerResponse addBlocker(String taskId, BlockerRequest request) {
        log.info("Adding blocker to task {}: {}", taskId, request.name());
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        Blocker blocker = new Blocker();
        blocker.setId(UUID.randomUUID().toString());
        blocker.setTask(task);
        blocker.setName(request.name());
        blocker.setStatus("ACTIVE");

        Blocker saved = blockerRepository.save(blocker);

        // Activity Logging
        logActivity(task.getId(), task.getSprint() != null ? task.getSprint().getId() : null,
                "BLOCKER_ADDED", "Blocker added: '" + request.name() + "'");

        return toResponse(saved);
    }

    @Override
    @Transactional
    public BlockerResponse resolveBlocker(String blockerId) {
        log.info("Resolving blocker: {}", blockerId);
        Blocker blocker = blockerRepository.findById(blockerId)
                .orElseThrow(() -> new ResourceNotFoundException("Blocker not found with id: " + blockerId));

        blocker.setStatus("RESOLVED");
        blocker.setResolvedAt(LocalDateTime.now());
        blocker.setResolvedBy(SecurityUtils.getCurrentUserId());

        Blocker saved = blockerRepository.save(blocker);
        Task task = saved.getTask();

        // Activity Logging
        logActivity(task.getId(), task.getSprint() != null ? task.getSprint().getId() : null,
                "BLOCKER_RESOLVED", "Blocker resolved: '" + saved.getName() + "'");

        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteBlocker(String blockerId) {
        log.info("Deleting blocker: {}", blockerId);
        Blocker blocker = blockerRepository.findById(blockerId)
                .orElseThrow(() -> new ResourceNotFoundException("Blocker not found with id: " + blockerId));

        blocker.softDelete(SecurityUtils.getCurrentUserId());
        blockerRepository.save(blocker);

        Task task = blocker.getTask();

        // Activity Logging
        logActivity(task.getId(), task.getSprint() != null ? task.getSprint().getId() : null,
                "BLOCKER_REMOVED", "Blocker removed: '" + blocker.getName() + "'");
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlockerResponse> getBlockersByTask(String taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found with id: " + taskId);
        }
        return blockerRepository.findByTaskId(taskId).stream()
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

    private BlockerResponse toResponse(Blocker blocker) {
        return new BlockerResponse(
                blocker.getId(),
                blocker.getTask().getId(),
                blocker.getName(),
                blocker.getStatus(),
                blocker.getResolvedAt(),
                blocker.getResolvedBy(),
                blocker.getCreatedAt(),
                blocker.getCreatedBy()
        );
    }
}
