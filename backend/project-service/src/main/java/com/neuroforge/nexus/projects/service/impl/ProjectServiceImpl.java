package com.neuroforge.nexus.projects.service.impl;

import com.neuroforge.nexus.shared.exception.ConflictException;
import com.neuroforge.nexus.shared.exception.ResourceNotFoundException;
import com.neuroforge.nexus.shared.util.SecurityUtils;
import com.neuroforge.nexus.projects.controller.mapper.ProjectMapper;
import com.neuroforge.nexus.projects.domain.Project;
import com.neuroforge.nexus.projects.domain.Team;
import com.neuroforge.nexus.projects.domain.User;
import com.neuroforge.nexus.projects.dto.ProjectRequest;
import com.neuroforge.nexus.projects.dto.ProjectResponse;
import com.neuroforge.nexus.projects.event.ProjectCreatedEvent;
import com.neuroforge.nexus.projects.event.ProjectEventPublisher;
import com.neuroforge.nexus.projects.repository.ProjectRepository;
import com.neuroforge.nexus.projects.repository.TeamRepository;
import com.neuroforge.nexus.projects.repository.UserRepository;
import com.neuroforge.nexus.projects.service.ProjectService;
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
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final ProjectMapper projectMapper;
    private final ProjectEventPublisher eventPublisher;

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        log.info("Creating project: {}", request.name());

        if (projectRepository.existsByName(request.name())) {
            throw new ConflictException("Project name already exists: " + request.name());
        }
        
        String keyUpper = request.key().toUpperCase();
        if (projectRepository.existsByKey(keyUpper)) {
            throw new ConflictException("Project key already exists: " + keyUpper);
        }

        User manager = userRepository.findById(request.managerId())
                .orElseGet(() -> {
                    log.warn("Manager user {} not found in local replica. Creating stub user to prevent failure.", request.managerId());
                    User stub = new User();
                    stub.setId(request.managerId());
                    stub.setUsername("synced_user_" + request.managerId().substring(0, Math.min(request.managerId().length(), 8)));
                    stub.setFirstName("Synced");
                    stub.setLastName("User");
                    return userRepository.save(stub);
                });

        Project project = new Project();
        project.setId(UUID.randomUUID().toString());
        project.setName(request.name());
        project.setKey(keyUpper);
        project.setDescription(request.description());
        project.setManager(manager);
        if (request.status() != null) {
            project.setStatus(request.status());
        }

        Project saved = projectRepository.save(project);

        // Publish project-created Kafka event
        eventPublisher.publishProjectCreated(new ProjectCreatedEvent(
                saved.getId(),
                saved.getName(),
                saved.getKey()
        ));

        return projectMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(String id, ProjectRequest request) {
        log.info("Updating project: {}", id);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        if (!project.getName().equals(request.name()) && projectRepository.existsByName(request.name())) {
            throw new ConflictException("Project name already exists: " + request.name());
        }
        
        String keyUpper = request.key().toUpperCase();
        if (!project.getKey().equals(keyUpper) && projectRepository.existsByKey(keyUpper)) {
            throw new ConflictException("Project key already exists: " + request.key());
        }

        User manager = userRepository.findById(request.managerId())
                .orElseGet(() -> {
                    log.warn("Manager user {} not found in local replica. Creating stub user to prevent failure.", request.managerId());
                    User stub = new User();
                    stub.setId(request.managerId());
                    stub.setUsername("synced_user_" + request.managerId().substring(0, Math.min(request.managerId().length(), 8)));
                    stub.setFirstName("Synced");
                    stub.setLastName("User");
                    return userRepository.save(stub);
                });

        project.setName(request.name());
        project.setKey(keyUpper);
        project.setDescription(request.description());
        project.setManager(manager);
        if (request.status() != null) {
            project.setStatus(request.status());
        }

        Project saved = projectRepository.save(project);
        return projectMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(String id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectByKey(String key) {
        Project project = projectRepository.findByKey(key.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with key: " + key));
        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(projectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteProject(String id) {
        log.info("Deleting project: {}", id);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        project.softDelete(SecurityUtils.getCurrentUserId());
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public ProjectResponse associateTeam(String projectId, String teamId) {
        log.info("Associating team {} with project {}", teamId, projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        Team team = teamRepository.findById(teamId)
                .orElseGet(() -> {
                    log.warn("Team {} not found in local replica. Creating stub to prevent foreign key failure.", teamId);
                    Team stub = new Team();
                    stub.setId(teamId);
                    stub.setName("Synced Team " + teamId.substring(0, 8));
                    stub.setCode("T" + teamId.substring(0, 3).toUpperCase());
                    return teamRepository.save(stub);
                });

        if (!project.getTeams().contains(team)) {
            project.getTeams().add(team);
            projectRepository.save(project);
        }

        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional
    public ProjectResponse disassociateTeam(String projectId, String teamId) {
        log.info("Disassociating team {} from project {}", teamId, projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        Team team = teamRepository.findById(teamId)
                .orElseGet(() -> {
                    log.warn("Team {} not found in local replica. Creating stub to prevent foreign key failure.", teamId);
                    Team stub = new Team();
                    stub.setId(teamId);
                    stub.setName("Synced Team " + teamId.substring(0, 8));
                    stub.setCode("T" + teamId.substring(0, 3).toUpperCase());
                    return teamRepository.save(stub);
                });

        if (project.getTeams().contains(team)) {
            project.getTeams().remove(team);
            projectRepository.save(project);
        }

        return projectMapper.toResponse(project);
    }
}
