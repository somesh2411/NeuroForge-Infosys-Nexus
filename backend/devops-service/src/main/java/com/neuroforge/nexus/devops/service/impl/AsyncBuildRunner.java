package com.neuroforge.nexus.devops.service.impl;

import com.neuroforge.nexus.devops.domain.*;
import com.neuroforge.nexus.devops.provider.PipelineProvider;
import com.neuroforge.nexus.devops.repository.BuildRepository;
import com.neuroforge.nexus.devops.repository.PipelineRepository;
import com.neuroforge.nexus.devops.repository.PipelineStageRepository;
import com.neuroforge.nexus.devops.service.AuditEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
public class AsyncBuildRunner {

    private static final Logger log = LoggerFactory.getLogger(AsyncBuildRunner.class);
    private final BuildRepository buildRepository;
    private final PipelineRepository pipelineRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final AuditEventService auditEventService;
    private final List<PipelineProvider> providers;
    private final Random random = new Random();

    public AsyncBuildRunner(BuildRepository buildRepository, 
                            PipelineRepository pipelineRepository, 
                            PipelineStageRepository pipelineStageRepository, 
                            AuditEventService auditEventService, 
                            List<PipelineProvider> providers) {
        this.buildRepository = buildRepository;
        this.pipelineRepository = pipelineRepository;
        this.pipelineStageRepository = pipelineStageRepository;
        this.auditEventService = auditEventService;
        this.providers = providers;
    }

    @Async
    @Transactional
    public void runBuildAsync(String buildId) {
        log.info("AsyncBuildRunner: Starting background pipeline run for build: {}", buildId);
        Build build = buildRepository.findById(buildId).orElse(null);
        if (build == null) {
            log.error("AsyncBuildRunner: Build {} not found", buildId);
            return;
        }

        Pipeline pipeline = build.getPipeline();
        pipeline.setStatus("RUNNING");
        pipelineRepository.save(pipeline);

        build.setStatus("RUNNING");
        build.setStartTime(LocalDateTime.now());
        buildRepository.save(build);

        auditEventService.logEvent("BUILD_STARTED", 
                "Build #" + build.getBuildNumber() + " execution started for pipeline '" + pipeline.getName() + "'.");

        // Fetch the matching PipelineProvider
        PipelineProvider provider = providers.stream()
                .filter(p -> p.getProviderType().equalsIgnoreCase(pipeline.getPipelineType()))
                .findFirst()
                .orElse(providers.stream()
                        .filter(p -> p.getProviderType().equalsIgnoreCase("MOCK"))
                        .findFirst()
                        .orElse(null));

        List<PipelineStage> stages = pipelineStageRepository.findByBuildId(buildId);
        boolean buildFailed = false;
        long totalDurationMs = 0;

        for (PipelineStage stage : stages) {
            if (buildFailed) {
                // If a previous stage failed, skip subsequent stages
                stage.setStatus("FAILED");
                stage.setStageLog("Stage skipped due to previous execution failure.");
                pipelineStageRepository.save(stage);
                continue;
            }

            // Transition stage to RUNNING
            stage.setStatus("RUNNING");
            stage.setStartTime(LocalDateTime.now());
            pipelineStageRepository.save(stage);
            auditEventService.logEvent("STAGE_STARTED", 
                    "Pipeline stage '" + stage.getName() + "' started for Build #" + build.getBuildNumber() + ".");

            // Simulate execution work
            try {
                long delay = 1500 + random.nextInt(1500); // 1.5s - 3.0s delay
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Stage sleep interrupted", e);
            }

            // Determine success/failure
            boolean stageSucceeded = true;
            
            // Artificial failure trigger logic:
            // 1. Branch name containing 'fail'
            // 2. 5% random failure chance on Test/Quality stages
            if (build.getBranch() != null && build.getBranch().toLowerCase().contains("fail")) {
                if (stage.getName().equalsIgnoreCase("Test") || stage.getName().equalsIgnoreCase("Code Quality")) {
                    stageSucceeded = false;
                }
            } else if (stage.getName().equalsIgnoreCase("Code Quality") && random.nextInt(100) < 5) {
                stageSucceeded = false; // 5% random chance of failing static analysis
            } else if (stage.getName().equalsIgnoreCase("Test") && random.nextInt(100) < 3) {
                stageSucceeded = false; // 3% random test failure
            }

            stage.setEndTime(LocalDateTime.now());
            long stageDuration = Duration.between(stage.getStartTime(), stage.getEndTime()).toMillis();
            stage.setDurationMs(stageDuration);
            totalDurationMs += stageDuration;

            if (stageSucceeded) {
                stage.setStatus("SUCCESS");
                // Fetch simulated logs from provider
                try {
                    String logs = provider != null ? provider.fetchStageLogs(pipeline, buildId, stage.getName()) : "Success.";
                    stage.setStageLog(logs);
                } catch (Exception e) {
                    stage.setStageLog("Stage completed successfully.");
                }
                pipelineStageRepository.save(stage);
                auditEventService.logEvent("STAGE_COMPLETED", 
                        "Pipeline stage '" + stage.getName() + "' completed with SUCCESS for Build #" + build.getBuildNumber() + ".");
            } else {
                stage.setStatus("FAILED");
                buildFailed = true;
                // Fetch failure logs
                String failureLogs = switch (stage.getName().toUpperCase()) {
                    case "TEST" -> """
                        [ERROR] Failed tests:
                        [ERROR]   com.neuroforge.nexus.devops.PipelineControllerTests.testPipelineTriggerFailure
                        [ERROR]   Run: 25, Failures: 1, Errors: 0, Skipped: 0
                        [ERROR] BUILD FAILURE
                        """;
                    case "CODE QUALITY" -> """
                        [SonarQube] Analyzing code...
                        [WARNING] Quality Gate: FAILED
                        [WARNING] Reason: Coverage is 76.5% which is below required 80.0% threshold.
                        [ERROR] Static analysis check failed.
                        """;
                    default -> "[ERROR] Stage failed due to runtime compilation/resource limits.";
                };
                stage.setStageLog(failureLogs);
                pipelineStageRepository.save(stage);
                auditEventService.logEvent("STAGE_FAILED", 
                        "Pipeline stage '" + stage.getName() + "' FAILED for Build #" + build.getBuildNumber() + ".");
            }
        }

        // Finalize build run
        build.setEndTime(LocalDateTime.now());
        build.setDurationMs(totalDurationMs);

        if (buildFailed) {
            build.setStatus("FAILED");
            build.setTestsTotal(20);
            build.setTestsPassed(18);
            build.setTestsFailed(2);
            buildRepository.save(build);

            pipeline.setStatus("FAILING");
            pipelineRepository.save(pipeline);

            auditEventService.logEvent("BUILD_FAILED", 
                    "Build #" + build.getBuildNumber() + " FAILED for pipeline '" + pipeline.getName() + "'.");
        } else {
            build.setStatus("SUCCESS");
            build.setTestsTotal(25);
            build.setTestsPassed(25);
            build.setTestsFailed(0);
            
            // Set dummy artifact metadata
            String tool = pipeline.getBuildTool();
            if (tool.equalsIgnoreCase("MAVEN")) {
                build.setArtifactName(pipeline.getName().toLowerCase().replace(" ", "-") + "-v1.0.0-b" + build.getBuildNumber() + ".jar");
                build.setArtifactSize(25600000L + random.nextInt(4000000)); // ~25MB - 29MB
            } else if (tool.equalsIgnoreCase("NPM")) {
                build.setArtifactName("dist-" + pipeline.getName().toLowerCase().replace(" ", "-") + "-b" + build.getBuildNumber() + ".tar.gz");
                build.setArtifactSize(4200000L + random.nextInt(800000)); // ~4MB
            } else {
                build.setArtifactName("image-" + pipeline.getName().toLowerCase().replace(" ", "-") + "-b" + build.getBuildNumber());
                build.setArtifactSize(180000000L + random.nextInt(20000000)); // ~180MB
            }

            buildRepository.save(build);

            pipeline.setStatus("SUCCESSFUL");
            pipelineRepository.save(pipeline);

            auditEventService.logEvent("BUILD_SUCCESSFUL", 
                    "Build #" + build.getBuildNumber() + " completed with SUCCESS for pipeline '" + pipeline.getName() + "'. Artifact: " + build.getArtifactName());
        }
    }
}
