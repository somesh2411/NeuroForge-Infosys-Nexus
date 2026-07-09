package com.neuroforge.nexus.sprints.service.impl;

import com.neuroforge.nexus.shared.exception.ResourceNotFoundException;
import com.neuroforge.nexus.shared.util.SecurityUtils;
import com.neuroforge.nexus.sprints.controller.mapper.SprintMapper;
import com.neuroforge.nexus.sprints.domain.Project;
import com.neuroforge.nexus.sprints.domain.Sprint;
import com.neuroforge.nexus.sprints.dto.SprintRequest;
import com.neuroforge.nexus.sprints.dto.SprintResponse;
import com.neuroforge.nexus.sprints.repository.ProjectRepository;
import com.neuroforge.nexus.sprints.repository.SprintRepository;
import com.neuroforge.nexus.sprints.service.SprintService;
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
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final SprintMapper sprintMapper;

    @Override
    @Transactional
    public SprintResponse createSprint(String projectId, SprintRequest request) {
        log.info("Creating sprint for project: {}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseGet(() -> {
                    log.warn("Project {} not found in local replica. Creating stub to prevent foreign key failure.", projectId);
                    Project stub = new Project();
                    stub.setId(projectId);
                    stub.setName("Synced Project " + projectId.substring(0, 8));
                    stub.setKey("PRJ");
                    return projectRepository.save(stub);
                });

        Sprint sprint = new Sprint();
        sprint.setId(UUID.randomUUID().toString());
        sprint.setProject(project);
        sprint.setName(request.name());
        sprint.setGoal(request.goal());
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());
        sprint.setCapacity(request.capacity());
        if (request.status() != null) {
            sprint.setStatus(request.status());
        }

        Sprint saved = sprintRepository.save(sprint);
        return sprintMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public SprintResponse updateSprint(String id, SprintRequest request) {
        log.info("Updating sprint: {}", id);
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id: " + id));

        sprint.setName(request.name());
        sprint.setGoal(request.goal());
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());
        sprint.setCapacity(request.capacity());
        if (request.status() != null) {
            sprint.setStatus(request.status());
        }

        Sprint saved = sprintRepository.save(sprint);
        return sprintMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SprintResponse getSprintById(String id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id: " + id));
        return sprintMapper.toResponse(sprint);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SprintResponse> getAllSprints() {
        return sprintRepository.findAll().stream()
                .map(sprintMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<SprintResponse> getSprintsByProject(String projectId) {
        if (!projectRepository.existsById(projectId)) {
            log.warn("Project {} not found in local replica during sprint list. Creating stub.", projectId);
            Project stub = new Project();
            stub.setId(projectId);
            stub.setName("Synced Project " + projectId.substring(0, 8));
            stub.setKey("PRJ");
            projectRepository.save(stub);
        }
        return sprintRepository.findByProjectId(projectId).stream()
                .map(sprintMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSprint(String id) {
        log.info("Deleting sprint: {}", id);
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id: " + id));

        sprint.softDelete(SecurityUtils.getCurrentUserId());
        sprintRepository.save(sprint);
    }
}
