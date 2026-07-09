package com.neuroforge.nexus.sprints.service.impl;

import com.neuroforge.nexus.shared.exception.ResourceNotFoundException;
import com.neuroforge.nexus.shared.util.SecurityUtils;
import com.neuroforge.nexus.sprints.domain.*;
import com.neuroforge.nexus.sprints.dto.TaskRequest;
import com.neuroforge.nexus.sprints.dto.TaskResponse;
import com.neuroforge.nexus.sprints.repository.*;
import com.neuroforge.nexus.sprints.service.TaskService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;

    @Override
    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        log.info("Creating task: {}", request.title());

        Project project = projectRepository.findById(request.projectId())
                .orElseGet(() -> {
                    log.warn("Project {} not found in local replica. Creating stub.", request.projectId());
                    Project stub = new Project();
                    stub.setId(request.projectId());
                    stub.setName("Synced Project " + request.projectId().substring(0, 8));
                    stub.setKey("PRJ");
                    return projectRepository.save(stub);
                });

        Sprint sprint = null;
        if (request.sprintId() != null && !request.sprintId().trim().isEmpty()) {
            sprint = sprintRepository.findById(request.sprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint not found: " + request.sprintId()));
            if (!sprint.getProject().getId().equals(project.getId())) {
                throw new IllegalArgumentException("Sprint does not belong to the selected project");
            }
        }

        User assignee = null;
        if (request.assignedDeveloperId() != null && !request.assignedDeveloperId().trim().isEmpty()) {
            assignee = userRepository.findById(request.assignedDeveloperId())
                    .orElseThrow(() -> new ResourceNotFoundException("Developer not found: " + request.assignedDeveloperId()));
        }

        Task task = new Task();
        task.setId(UUID.randomUUID().toString());
        task.setProject(project);
        task.setSprint(sprint);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setAssignedDeveloper(assignee);
        task.setPriority(request.priority() != null ? request.priority() : "MEDIUM");
        task.setStatus(request.status() != null ? request.status() : "TO_DO");
        task.setStoryPoints(request.storyPoints());
        task.setDueDate(request.dueDate());
        task.setLabels(request.labels());
        task.setEstimatedHours(request.estimatedHours() != null ? request.estimatedHours() : 0.0);
        task.setActualHours(request.actualHours() != null ? request.actualHours() : 0.0);

        Task saved = taskRepository.save(task);

        // Activity Logs
        logActivity(saved.getId(), saved.getSprint() != null ? saved.getSprint().getId() : null,
                "TASK_CREATED", "Task '" + saved.getTitle() + "' was created.");

        if (assignee != null) {
            logActivity(saved.getId(), saved.getSprint() != null ? saved.getSprint().getId() : null,
                    "TASK_ASSIGNED", "Task assigned to " + assignee.getFirstName() + " " + assignee.getLastName());
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(String id, TaskRequest request) {
        log.info("Updating task: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        User oldAssignee = task.getAssignedDeveloper();
        User newAssignee = null;
        if (request.assignedDeveloperId() != null && !request.assignedDeveloperId().trim().isEmpty()) {
            newAssignee = userRepository.findById(request.assignedDeveloperId())
                    .orElseThrow(() -> new ResourceNotFoundException("Developer not found: " + request.assignedDeveloperId()));
        }

        Sprint sprint = null;
        if (request.sprintId() != null && !request.sprintId().trim().isEmpty()) {
            sprint = sprintRepository.findById(request.sprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint not found: " + request.sprintId()));
            if (!sprint.getProject().getId().equals(task.getProject().getId())) {
                throw new IllegalArgumentException("Sprint does not belong to the selected project");
            }
        }

        String oldStatus = task.getStatus();
        String newStatus = request.status() != null ? request.status() : "TO_DO";

        task.setSprint(sprint);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setAssignedDeveloper(newAssignee);
        task.setPriority(request.priority() != null ? request.priority() : "MEDIUM");
        task.setStatus(newStatus);
        task.setStoryPoints(request.storyPoints());
        task.setDueDate(request.dueDate());
        task.setLabels(request.labels());
        task.setEstimatedHours(request.estimatedHours() != null ? request.estimatedHours() : 0.0);
        task.setActualHours(request.actualHours() != null ? request.actualHours() : 0.0);

        Task saved = taskRepository.save(task);

        // Activity Logs
        if (!oldStatus.equalsIgnoreCase(newStatus)) {
            logActivity(saved.getId(), saved.getSprint() != null ? saved.getSprint().getId() : null,
                    "TASK_STATUS_CHANGED", "Task status changed from " + oldStatus + " to " + newStatus);
        }

        boolean assigneeChanged = (oldAssignee == null && newAssignee != null) ||
                (oldAssignee != null && newAssignee == null) ||
                (oldAssignee != null && newAssignee != null && !oldAssignee.getId().equals(newAssignee.getId()));

        if (assigneeChanged) {
            String msg = newAssignee != null ? 
                    "Task assigned to " + newAssignee.getFirstName() + " " + newAssignee.getLastName() : 
                    "Task unassigned";
            logActivity(saved.getId(), saved.getSprint() != null ? saved.getSprint().getId() : null,
                    "TASK_ASSIGNED", msg);
        }

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(String id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return toResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasks(String projectId, String sprintId, String developerId,
                                       String priority, String status, String search,
                                       String sortBy, String sortDir, int page, int size) {
        log.info("Fetching paginated tasks with filters");

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir != null ? sortDir : "DESC"), sortBy != null ? sortBy : "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Task> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (projectId != null && !projectId.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("project").get("id"), projectId));
            }

            if (sprintId != null && !sprintId.trim().isEmpty()) {
                if (sprintId.equalsIgnoreCase("backlog")) {
                    predicates.add(cb.isNull(root.get("sprint")));
                } else {
                    predicates.add(cb.equal(root.get("sprint").get("id"), sprintId));
                }
            }

            if (developerId != null && !developerId.trim().isEmpty()) {
                if (developerId.equalsIgnoreCase("unassigned")) {
                    predicates.add(cb.isNull(root.get("assignedDeveloper")));
                } else {
                    predicates.add(cb.equal(root.get("assignedDeveloper").get("id"), developerId));
                }
            }

            if (priority != null && !priority.trim().isEmpty()) {
                predicates.add(cb.equal(cb.upper(root.get("priority")), priority.toUpperCase()));
            }

            if (status != null && !status.trim().isEmpty()) {
                predicates.add(cb.equal(cb.upper(root.get("status")), status.toUpperCase()));
            }

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), searchPattern);
                Predicate descLike = cb.like(cb.lower(root.get("description")), searchPattern);
                predicates.add(cb.or(titleLike, descLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return taskRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksBySprint(String sprintId) {
        return taskRepository.findBySprintId(sprintId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getBacklogTasks(String projectId) {
        return taskRepository.findBacklogTasksByProjectId(projectId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskResponse updateTaskStatus(String id, String status, int version) {
        log.info("Updating status for task {} to {}", id, status);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        if (task.getVersion() != version) {
            throw new ObjectOptimisticLockingFailureException(Task.class, id);
        }

        String oldStatus = task.getStatus();
        task.setStatus(status.toUpperCase());
        Task saved = taskRepository.save(task);

        if (!oldStatus.equalsIgnoreCase(status)) {
            logActivity(saved.getId(), saved.getSprint() != null ? saved.getSprint().getId() : null,
                    "TASK_STATUS_CHANGED", "Task status dragged from " + oldStatus + " to " + status);
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteTask(String id) {
        log.info("Soft deleting task: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        task.softDelete(SecurityUtils.getCurrentUserId());
        taskRepository.save(task);

        logActivity(task.getId(), task.getSprint() != null ? task.getSprint().getId() : null,
                "TASK_DELETED", "Task '" + task.getTitle() + "' was deleted.");
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

    private TaskResponse toResponse(Task task) {
        String developerId = task.getAssignedDeveloper() != null ? task.getAssignedDeveloper().getId() : null;
        String developerName = task.getAssignedDeveloper() != null ? 
                task.getAssignedDeveloper().getFirstName() + " " + task.getAssignedDeveloper().getLastName() : "Unassigned";
        if (task.getAssignedDeveloper() != null && developerName.trim().isEmpty()) {
            developerName = task.getAssignedDeveloper().getUsername();
        }

        String sprintId = task.getSprint() != null ? task.getSprint().getId() : null;
        String sprintName = task.getSprint() != null ? task.getSprint().getName() : null;

        return new TaskResponse(
                task.getId(),
                task.getProject().getId(),
                sprintId,
                sprintName,
                task.getTitle(),
                task.getDescription(),
                developerId,
                developerName,
                task.getPriority(),
                task.getStatus(),
                task.getStoryPoints(),
                task.getDueDate(),
                task.getLabels(),
                task.getEstimatedHours(),
                task.getActualHours(),
                task.getVersion(),
                task.getCreatedAt(),
                task.getCreatedBy(),
                task.getUpdatedAt(),
                task.getUpdatedBy()
        );
    }
}
