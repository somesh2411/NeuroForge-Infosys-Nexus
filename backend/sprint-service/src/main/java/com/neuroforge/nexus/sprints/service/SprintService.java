package com.neuroforge.nexus.sprints.service;

import com.neuroforge.nexus.sprints.dto.SprintRequest;
import com.neuroforge.nexus.sprints.dto.SprintResponse;

import java.util.List;

public interface SprintService {
    SprintResponse createSprint(String projectId, SprintRequest request);
    SprintResponse updateSprint(String id, SprintRequest request);
    SprintResponse getSprintById(String id);
    List<SprintResponse> getSprintsByProject(String projectId);
    List<SprintResponse> getAllSprints();
    void deleteSprint(String id);
}
