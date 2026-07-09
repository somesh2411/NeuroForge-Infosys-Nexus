package com.neuroforge.nexus.sprints.service;

import com.neuroforge.nexus.sprints.dto.TaskRequest;
import com.neuroforge.nexus.sprints.dto.TaskResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TaskService {
    TaskResponse createTask(TaskRequest request);
    TaskResponse updateTask(String id, TaskRequest request);
    TaskResponse getTaskById(String id);
    Page<TaskResponse> getTasks(String projectId, String sprintId, String developerId, 
                                 String priority, String status, String search,
                                 String sortBy, String sortDir, int page, int size);
    List<TaskResponse> getTasksBySprint(String sprintId);
    List<TaskResponse> getBacklogTasks(String projectId);
    TaskResponse updateTaskStatus(String id, String status, int version);
    void deleteTask(String id);
}
