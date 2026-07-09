package com.neuroforge.nexus.projects.service;

import com.neuroforge.nexus.projects.dto.ProjectRequest;
import com.neuroforge.nexus.projects.dto.ProjectResponse;

import java.util.List;

public interface ProjectService {
    ProjectResponse createProject(ProjectRequest request);
    ProjectResponse updateProject(String id, ProjectRequest request);
    ProjectResponse getProjectById(String id);
    ProjectResponse getProjectByKey(String key);
    List<ProjectResponse> getAllProjects();
    void deleteProject(String id);
    ProjectResponse associateTeam(String projectId, String teamId);
    ProjectResponse disassociateTeam(String projectId, String teamId);
}
