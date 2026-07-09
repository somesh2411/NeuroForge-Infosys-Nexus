package com.neuroforge.nexus.sprints.service.impl;

import com.neuroforge.nexus.shared.exception.ResourceNotFoundException;
import com.neuroforge.nexus.sprints.domain.*;
import com.neuroforge.nexus.sprints.dto.BurndownPoint;
import com.neuroforge.nexus.sprints.dto.SprintMetricsResponse;
import com.neuroforge.nexus.sprints.dto.VelocityPoint;
import com.neuroforge.nexus.sprints.repository.*;
import com.neuroforge.nexus.sprints.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;
    private final ActivityLogRepository activityLogRepository;

    @Override
    @Transactional(readOnly = true)
    public SprintMetricsResponse getSprintMetrics(String sprintId) {
        log.info("Calculating sprint metrics for sprint: {}", sprintId);
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id: " + sprintId));

        List<Task> tasks = taskRepository.findBySprintId(sprintId);

        long totalTasks = tasks.size();
        long completedTasks = tasks.stream()
                .filter(t -> t.getStatus().equalsIgnoreCase("DONE"))
                .count();
        long remainingTasks = totalTasks - completedTasks;

        int totalStoryPoints = tasks.stream()
                .mapToInt(Task::getStoryPoints)
                .sum();
        int completedStoryPoints = tasks.stream()
                .filter(t -> t.getStatus().equalsIgnoreCase("DONE"))
                .mapToInt(Task::getStoryPoints)
                .sum();
        int remainingStoryPoints = totalStoryPoints - completedStoryPoints;

        double progressPercentage = totalStoryPoints > 0 ? 
                (completedStoryPoints * 100.0) / totalStoryPoints : 0.0;

        return new SprintMetricsResponse(
                sprint.getId(),
                sprint.getName(),
                sprint.getGoal(),
                sprint.getStatus(),
                totalTasks,
                completedTasks,
                remainingTasks,
                totalStoryPoints,
                completedStoryPoints,
                remainingStoryPoints,
                progressPercentage
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BurndownPoint> getBurndownData(String sprintId) {
        log.info("Calculating burndown chart data for sprint: {}", sprintId);
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id: " + sprintId));

        List<Task> tasks = taskRepository.findBySprintId(sprintId);
        int totalStoryPoints = tasks.stream().mapToInt(Task::getStoryPoints).sum();

        LocalDateTime start = sprint.getStartDate() != null ? sprint.getStartDate() : LocalDateTime.now();
        LocalDateTime end = sprint.getEndDate() != null ? sprint.getEndDate() : LocalDateTime.now().plusDays(14);

        long totalDays = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate()) + 1;
        if (totalDays <= 0) {
            totalDays = 1;
        }

        // Cap at 30 days to prevent oversized loops in edge cases
        if (totalDays > 30) {
            totalDays = 30;
        }

        // Gather status change logs for DONE tasks in this sprint
        List<ActivityLog> logs = activityLogRepository.findBySprintIdOrderByTimestampDesc(sprintId);
        Map<String, LocalDate> taskCompletionDates = new HashMap<>();

        for (ActivityLog logEntry : logs) {
            if (logEntry.getTaskId() != null && logEntry.getEventType().equalsIgnoreCase("TASK_STATUS_CHANGED") &&
                    logEntry.getMessage().toLowerCase().contains("to done")) {
                // Keep the earliest completion date found in logs
                LocalDate completionDate = logEntry.getTimestamp().toLocalDate();
                taskCompletionDates.merge(logEntry.getTaskId(), completionDate, 
                        (oldDate, newDate) -> oldDate.isBefore(newDate) ? oldDate : newDate);
            }
        }

        // Also check task updated_at if no logs are found but task is currently DONE
        for (Task task : tasks) {
            if (task.getStatus().equalsIgnoreCase("DONE") && !taskCompletionDates.containsKey(task.getId())) {
                LocalDate date = task.getUpdatedAt() != null ? 
                        task.getUpdatedAt().toLocalDate() : task.getCreatedAt().toLocalDate();
                taskCompletionDates.put(task.getId(), date);
            }
        }

        List<BurndownPoint> points = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        for (int i = 0; i < totalDays; i++) {
            LocalDate currentDay = start.toLocalDate().plusDays(i);
            String label = currentDay.format(formatter);

            // Ideal burndown remaining
            double idealRemaining = totalStoryPoints - (i * ((double) totalStoryPoints / (totalDays - 1)));
            if (idealRemaining < 0 || totalDays == 1) {
                idealRemaining = 0.0;
            }

            // Actual remaining story points on this day
            int completedByDay = 0;
            for (Task task : tasks) {
                LocalDate compDate = taskCompletionDates.get(task.getId());
                if (compDate != null && (compDate.isBefore(currentDay) || compDate.isEqual(currentDay))) {
                    completedByDay += task.getStoryPoints();
                }
            }
            double actualRemaining = totalStoryPoints - completedByDay;
            if (actualRemaining < 0) {
                actualRemaining = 0.0;
            }

            // If the day is in the future, don't display actual remaining points on the burndown line
            if (currentDay.isAfter(LocalDate.now())) {
                points.add(new BurndownPoint(label, idealRemaining, -1.0));
            } else {
                points.add(new BurndownPoint(label, idealRemaining, actualRemaining));
            }
        }

        return points;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VelocityPoint> getProjectVelocity(String projectId) {
        log.info("Calculating project velocity for project: {}", projectId);
        List<Sprint> sprints = sprintRepository.findByProjectId(projectId).stream()
                .filter(s -> s.getStatus().equalsIgnoreCase("COMPLETED"))
                .sorted(Comparator.comparing(Sprint::getStartDate))
                .collect(Collectors.toList());

        List<VelocityPoint> velocityData = new ArrayList<>();

        for (Sprint sprint : sprints) {
            List<Task> tasks = taskRepository.findBySprintId(sprint.getId());
            int completedStoryPoints = tasks.stream()
                    .filter(t -> t.getStatus().equalsIgnoreCase("DONE"))
                    .mapToInt(Task::getStoryPoints)
                    .sum();

            velocityData.add(new VelocityPoint(sprint.getId(), sprint.getName(), completedStoryPoints));
        }

        return velocityData;
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.neuroforge.nexus.sprints.dto.ActivityLogResponse> getTaskActivity(String taskId) {
        return activityLogRepository.findByTaskIdOrderByTimestampDesc(taskId).stream()
                .map(this::toActivityResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.neuroforge.nexus.sprints.dto.ActivityLogResponse> getSprintActivity(String sprintId) {
        return activityLogRepository.findBySprintIdOrderByTimestampDesc(sprintId).stream()
                .map(this::toActivityResponse)
                .collect(Collectors.toList());
    }

    private com.neuroforge.nexus.sprints.dto.ActivityLogResponse toActivityResponse(ActivityLog log) {
        return new com.neuroforge.nexus.sprints.dto.ActivityLogResponse(
                log.getId(),
                log.getTaskId(),
                log.getSprintId(),
                log.getEventType(),
                log.getMessage(),
                log.getActor(),
                log.getTimestamp()
        );
    }
}
