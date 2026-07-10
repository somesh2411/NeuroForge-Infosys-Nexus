package com.neuroforge.nexus.devops.service.impl;

import com.neuroforge.nexus.shared.exception.ResourceNotFoundException;
import com.neuroforge.nexus.devops.domain.Pipeline;
import com.neuroforge.nexus.devops.domain.Repository;
import com.neuroforge.nexus.devops.dto.PipelineRequest;
import com.neuroforge.nexus.devops.dto.PipelineResponse;
import com.neuroforge.nexus.devops.repository.PipelineRepository;
import com.neuroforge.nexus.devops.repository.RepositoryRepository;
import com.neuroforge.nexus.devops.service.AuditEventService;
import com.neuroforge.nexus.devops.service.PipelineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PipelineServiceImpl implements PipelineService {

    private static final Logger log = LoggerFactory.getLogger(PipelineServiceImpl.class);
    private final PipelineRepository pipelineRepository;
    private final RepositoryRepository repositoryRepository;
    private final AuditEventService auditEventService;

    public PipelineServiceImpl(PipelineRepository pipelineRepository, 
                               RepositoryRepository repositoryRepository, 
                               AuditEventService auditEventService) {
        this.pipelineRepository = pipelineRepository;
        this.repositoryRepository = repositoryRepository;
        this.auditEventService = auditEventService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PipelineResponse> getPipelinesByProject(String projectId) {
        return pipelineRepository.findByProjectId(projectId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PipelineResponse getPipelineById(String id) {
        return pipelineRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline not found with id: " + id));
    }

    @Override
    @Transactional
    public PipelineResponse createPipeline(PipelineRequest request) {
        log.info("Creating pipeline '{}' for project '{}'", request.name(), request.projectId());

        // Resolve or create Git Repository reference
        Repository repo = null;
        if (request.repositoryUrl() != null && !request.repositoryUrl().isBlank()) {
            repo = repositoryRepository.findByUrl(request.repositoryUrl())
                    .orElseGet(() -> {
                        Repository newRepo = Repository.builder()
                                .id("repo-" + UUID.randomUUID().toString().substring(0, 8))
                                .name(request.repositoryName() != null ? request.repositoryName() : "git-repo")
                                .url(request.repositoryUrl())
                                .defaultBranch(request.branch() != null ? request.branch() : "main")
                                .lastCommitId("c-" + UUID.randomUUID().toString().substring(0, 8))
                                .lastCommitMessage("Initial commit")
                                .lastCommitAuthor("GitSystem")
                                .lastCommitAt(LocalDateTime.now())
                                .build();
                        return repositoryRepository.save(newRepo);
                    });
        }

        // Apply templates configurations
        String buildTool = request.buildTool();
        String pipelineTemplate = request.pipelineTemplate();
        if (pipelineTemplate != null && !pipelineTemplate.isBlank()) {
            buildTool = switch (pipelineTemplate.toUpperCase()) {
                case "JAVA_MAVEN", "SPRING_BOOT" -> "MAVEN";
                case "ANGULAR" -> "NPM";
                case "DOCKER" -> "DOCKER";
                default -> buildTool;
            };
        }

        Pipeline pipeline = Pipeline.builder()
                .id("pipe-" + UUID.randomUUID().toString().substring(0, 8))
                .name(request.name())
                .projectId(request.projectId())
                .repository(repo)
                .branch(request.branch() != null && !request.branch().isBlank() ? request.branch() : "main")
                .buildTool(buildTool)
                .pipelineType(request.pipelineType() != null ? request.pipelineType().toUpperCase() : "MOCK")
                .pipelineTemplate(pipelineTemplate)
                .jenkinsJobName(request.jenkinsJobName())
                .githubWorkflowPath(request.githubWorkflowPath())
                .enabled(request.enabled())
                .status("IDLE")
                .build();

        Pipeline saved = pipelineRepository.save(pipeline);
        auditEventService.logEvent("PIPELINE_CREATED", 
                "Pipeline '" + pipeline.getName() + "' created for project ID " + pipeline.getProjectId() + " using template: " + (pipeline.getPipelineTemplate() != null ? pipeline.getPipelineTemplate() : "NONE"));

        return toResponse(saved);
    }

    @Override
    @Transactional
    public PipelineResponse updatePipeline(String id, PipelineRequest request) {
        log.info("Updating pipeline: {}", id);
        Pipeline pipeline = pipelineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline not found with id: " + id));

        pipeline.setName(request.name());
        pipeline.setBranch(request.branch());
        pipeline.setBuildTool(request.buildTool());
        pipeline.setPipelineType(request.pipelineType().toUpperCase());
        pipeline.setPipelineTemplate(request.pipelineTemplate());
        pipeline.setJenkinsJobName(request.jenkinsJobName());
        pipeline.setGithubWorkflowPath(request.githubWorkflowPath());
        pipeline.setEnabled(request.enabled());

        // Update repository url if it changes
        if (request.repositoryUrl() != null && !request.repositoryUrl().isBlank()) {
            if (pipeline.getRepository() == null || !pipeline.getRepository().getUrl().equalsIgnoreCase(request.repositoryUrl())) {
                Repository repo = repositoryRepository.findByUrl(request.repositoryUrl())
                        .orElseGet(() -> {
                            Repository newRepo = Repository.builder()
                                    .id("repo-" + UUID.randomUUID().toString().substring(0, 8))
                                    .name(request.repositoryName() != null ? request.repositoryName() : "git-repo")
                                    .url(request.repositoryUrl())
                                    .defaultBranch(request.branch() != null ? request.branch() : "main")
                                    .lastCommitId("c-" + UUID.randomUUID().toString().substring(0, 8))
                                    .lastCommitMessage("Initial commit")
                                    .lastCommitAuthor("GitSystem")
                                    .lastCommitAt(LocalDateTime.now())
                                    .build();
                            return repositoryRepository.save(newRepo);
                        });
                pipeline.setRepository(repo);
            }
        }

        Pipeline saved = pipelineRepository.save(pipeline);
        auditEventService.logEvent("PIPELINE_UPDATED", "Pipeline '" + pipeline.getName() + "' configuration updated.");
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deletePipeline(String id) {
        log.info("Deleting pipeline: {}", id);
        Pipeline pipeline = pipelineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline not found with id: " + id));

        pipelineRepository.delete(pipeline);
        auditEventService.logEvent("PIPELINE_DELETED", "Pipeline '" + pipeline.getName() + "' deleted.");
    }

    @Override
    @Transactional
    public PipelineResponse togglePipeline(String id, boolean enabled) {
        log.info("Toggling pipeline {} state to {}", id, enabled);
        Pipeline pipeline = pipelineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline not found with id: " + id));

        pipeline.setEnabled(enabled);
        Pipeline saved = pipelineRepository.save(pipeline);
        auditEventService.logEvent("PIPELINE_STATE_CHANGED", 
                "Pipeline '" + pipeline.getName() + "' " + (enabled ? "ENABLED" : "DISABLED"));
        return toResponse(saved);
    }

    private PipelineResponse toResponse(Pipeline p) {
        return new PipelineResponse(
                p.getId(),
                p.getName(),
                p.getProjectId(),
                p.getRepository() != null ? p.getRepository().getId() : null,
                p.getRepository() != null ? p.getRepository().getName() : null,
                p.getRepository() != null ? p.getRepository().getUrl() : null,
                p.getBranch(),
                p.getBuildTool(),
                p.getPipelineType(),
                p.getPipelineTemplate(),
                p.getJenkinsJobName(),
                p.getGithubWorkflowPath(),
                p.isEnabled(),
                p.getStatus(),
                p.getCreatedAt(),
                p.getCreatedBy()
        );
    }
}
