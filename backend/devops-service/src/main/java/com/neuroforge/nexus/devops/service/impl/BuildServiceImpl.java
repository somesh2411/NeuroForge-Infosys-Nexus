package com.neuroforge.nexus.devops.service.impl;

import com.neuroforge.nexus.shared.exception.ResourceNotFoundException;
import com.neuroforge.nexus.shared.util.SecurityUtils;
import com.neuroforge.nexus.devops.domain.*;
import com.neuroforge.nexus.devops.dto.BuildCompareResponse;
import com.neuroforge.nexus.devops.dto.BuildResponse;
import com.neuroforge.nexus.devops.dto.PipelineStageResponse;
import com.neuroforge.nexus.devops.repository.BuildRepository;
import com.neuroforge.nexus.devops.repository.PipelineRepository;
import com.neuroforge.nexus.devops.repository.PipelineStageRepository;
import com.neuroforge.nexus.devops.service.AuditEventService;
import com.neuroforge.nexus.devops.service.BuildService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BuildServiceImpl implements BuildService {

    private static final Logger log = LoggerFactory.getLogger(BuildServiceImpl.class);
    private final BuildRepository buildRepository;
    private final PipelineRepository pipelineRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final AsyncBuildRunner asyncBuildRunner;
    private final AuditEventService auditEventService;

    public BuildServiceImpl(BuildRepository buildRepository, 
                            PipelineRepository pipelineRepository, 
                            PipelineStageRepository pipelineStageRepository, 
                            AsyncBuildRunner asyncBuildRunner, 
                            AuditEventService auditEventService) {
        this.buildRepository = buildRepository;
        this.pipelineRepository = pipelineRepository;
        this.pipelineStageRepository = pipelineStageRepository;
        this.asyncBuildRunner = asyncBuildRunner;
        this.auditEventService = auditEventService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BuildResponse> getBuildsByPipeline(String pipelineId) {
        return buildRepository.findByPipelineIdOrderByBuildNumberDesc(pipelineId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BuildResponse getBuildById(String id) {
        return buildRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Build not found with id: " + id));
    }

    @Override
    @Transactional
    public BuildResponse triggerBuild(String pipelineId, String triggerType) {
        log.info("Triggering build for pipeline: {}", pipelineId);
        Pipeline pipeline = pipelineRepository.findById(pipelineId)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline not found with id: " + pipelineId));

        if (!pipeline.isEnabled()) {
            throw new IllegalArgumentException("Pipeline is disabled and cannot execute builds.");
        }

        // Determine next build number
        Integer nextBuildNumber = buildRepository.findFirstByPipelineIdOrderByBuildNumberDesc(pipelineId)
                .map(b -> b.getBuildNumber() + 1)
                .orElse(1);

        String triggeredBy = "MANUAL";
        try {
            String currentUser = SecurityUtils.getCurrentUsername();
            if (currentUser != null && !currentUser.isBlank()) {
                triggeredBy = currentUser;
            }
        } catch (Exception e) {
            log.debug("Unauthenticated trigger context. Defaulting to MANUAL.");
        }

        // Build Record
        Build build = Build.builder()
                .id("build-" + UUID.randomUUID().toString().substring(0, 8))
                .pipeline(pipeline)
                .buildNumber(nextBuildNumber)
                .commitId(pipeline.getRepository() != null && pipeline.getRepository().getLastCommitId() != null 
                        ? pipeline.getRepository().getLastCommitId() : "c-" + UUID.randomUUID().toString().substring(0, 8))
                .branch(pipeline.getBranch())
                .status("QUEUED")
                .triggerType(triggerType != null ? triggerType.toUpperCase() : "MANUAL")
                .triggeredBy(triggeredBy)
                .build();

        Build savedBuild = buildRepository.save(build);

        // Pre-create Pipeline Stages
        String[] stageNames = {"Build", "Test", "Code Quality", "Docker Build", "Deploy"};
        for (String name : stageNames) {
            PipelineStage stage = PipelineStage.builder()
                    .id("stage-" + UUID.randomUUID().toString().substring(0, 8))
                    .build(savedBuild)
                    .name(name)
                    .status("QUEUED")
                    .build();
            pipelineStageRepository.save(stage);
        }

        auditEventService.logEvent("BUILD_TRIGGERED", 
                "Build #" + nextBuildNumber + " triggered by @" + triggeredBy + " on branch " + pipeline.getBranch());

        // Asynchronously execute stages in separate thread
        asyncBuildRunner.runBuildAsync(savedBuild.getId());

        return toResponse(savedBuild);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PipelineStageResponse> getBuildStages(String buildId) {
        return pipelineStageRepository.findByBuildId(buildId).stream()
                .map(s -> new PipelineStageResponse(
                        s.getId(),
                        s.getBuild().getId(),
                        s.getName(),
                        s.getStatus(),
                        s.getStartTime(),
                        s.getEndTime(),
                        s.getDurationMs(),
                        s.getStageLog()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BuildCompareResponse compareBuilds(String buildIdA, String buildIdB) {
        log.info("Comparing build {} vs {}", buildIdA, buildIdB);
        Build buildA = buildRepository.findById(buildIdA)
                .orElseThrow(() -> new ResourceNotFoundException("Build A not found with id: " + buildIdA));
        Build buildB = buildRepository.findById(buildIdB)
                .orElseThrow(() -> new ResourceNotFoundException("Build B not found with id: " + buildIdB));

        boolean statusMatch = buildA.getStatus().equalsIgnoreCase(buildB.getStatus());
        long durationDiff = Math.abs((buildA.getDurationMs() != null ? buildA.getDurationMs() : 0) - 
                                     (buildB.getDurationMs() != null ? buildB.getDurationMs() : 0));
        boolean commitMatch = buildA.getCommitId() != null && buildA.getCommitId().equalsIgnoreCase(buildB.getCommitId());
        int testDiff = Math.abs(buildA.getTestsPassed() - buildB.getTestsPassed());

        String deploymentNote = "Both builds have no recorded deployments.";
        if (buildA.getStatus().equalsIgnoreCase("SUCCESS") && !buildB.getStatus().equalsIgnoreCase("SUCCESS")) {
            deploymentNote = "Build #" + buildA.getBuildNumber() + " was successful and is deployable, while Build #" + buildB.getBuildNumber() + " is marked as " + buildB.getStatus();
        } else if (!buildA.getStatus().equalsIgnoreCase("SUCCESS") && buildB.getStatus().equalsIgnoreCase("SUCCESS")) {
            deploymentNote = "Build #" + buildB.getBuildNumber() + " is deployable, while Build #" + buildA.getBuildNumber() + " failed quality gates.";
        } else if (buildA.getStatus().equalsIgnoreCase("SUCCESS") && buildB.getStatus().equalsIgnoreCase("SUCCESS")) {
            deploymentNote = "Both builds are deployable. Build #" + buildB.getBuildNumber() + " has " + (buildB.getTestsPassed() - buildA.getTestsPassed() >= 0 ? "more or equal" : "fewer") + " passing unit tests than Build #" + buildA.getBuildNumber();
        }

        return new BuildCompareResponse(
                toResponse(buildA),
                toResponse(buildB),
                statusMatch,
                durationDiff,
                commitMatch,
                testDiff,
                deploymentNote
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BuildResponse> getRecentProjectBuilds(String projectId) {
        return buildRepository.findTop10ByPipelineProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private BuildResponse toResponse(Build b) {
        return new BuildResponse(
                b.getId(),
                b.getPipeline().getId(),
                b.getPipeline().getName(),
                b.getBuildNumber(),
                b.getCommitId(),
                b.getBranch(),
                b.getStatus(),
                b.getTriggerType(),
                b.getTriggeredBy(),
                b.getStartTime(),
                b.getEndTime(),
                b.getDurationMs(),
                b.getArtifactName(),
                b.getArtifactSize(),
                b.getTestsTotal(),
                b.getTestsPassed(),
                b.getTestsFailed(),
                b.getCreatedAt()
        );
    }
}
