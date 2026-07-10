package com.neuroforge.nexus.devops.service.impl;

import com.neuroforge.nexus.devops.domain.*;
import com.neuroforge.nexus.devops.dto.DevOpsMetricsResponse;
import com.neuroforge.nexus.devops.repository.BuildRepository;
import com.neuroforge.nexus.devops.repository.DeploymentRepository;
import com.neuroforge.nexus.devops.repository.EnvironmentRepository;
import com.neuroforge.nexus.devops.repository.ReleaseRepository;
import com.neuroforge.nexus.devops.service.DevOpsAnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DevOpsAnalyticsServiceImpl implements DevOpsAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(DevOpsAnalyticsServiceImpl.class);
    private final BuildRepository buildRepository;
    private final DeploymentRepository deploymentRepository;
    private final ReleaseRepository releaseRepository;
    private final EnvironmentRepository environmentRepository;

    public DevOpsAnalyticsServiceImpl(BuildRepository buildRepository, 
                                     DeploymentRepository deploymentRepository, 
                                     ReleaseRepository releaseRepository, 
                                     EnvironmentRepository environmentRepository) {
        this.buildRepository = buildRepository;
        this.deploymentRepository = deploymentRepository;
        this.releaseRepository = releaseRepository;
        this.environmentRepository = environmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DevOpsMetricsResponse getProjectDevOpsMetrics(String projectId) {
        log.info("Calculating DevOps analytics for project ID {}", projectId);

        // Fetch builds
        List<Build> builds = buildRepository.findAll().stream()
                .filter(b -> b.getPipeline().getProjectId().equalsIgnoreCase(projectId))
                .collect(Collectors.toList());

        long totalBuilds = builds.size();
        long successfulBuilds = builds.stream().filter(b -> b.getStatus().equalsIgnoreCase("SUCCESS")).count();
        long failedBuilds = builds.stream().filter(b -> b.getStatus().equalsIgnoreCase("FAILED")).count();
        long runningBuilds = builds.stream().filter(b -> b.getStatus().equalsIgnoreCase("RUNNING")).count();

        double buildSuccessRate = totalBuilds > 0 ? (successfulBuilds * 100.0) / totalBuilds : 0.0;

        double averageBuildDuration = builds.stream()
                .filter(b -> b.getStatus().equalsIgnoreCase("SUCCESS") && b.getDurationMs() != null)
                .mapToLong(Build::getDurationMs)
                .average()
                .orElse(0.0) / 1000.0; // convert to seconds

        // Fetch deployments
        List<Deployment> deployments = deploymentRepository.findByBuildPipelineProjectIdOrderByDeployedAtDesc(projectId);
        long totalDeployments = deployments.size();
        long successfulDeployments = deployments.stream().filter(d -> d.getStatus().equalsIgnoreCase("SUCCESSFUL")).count();
        double deploymentSuccessRate = totalDeployments > 0 ? (successfulDeployments * 100.0) / totalDeployments : 0.0;

        // Fetch releases
        long totalReleases = releaseRepository.findByBuildPipelineProjectIdOrderByReleasedAtDesc(projectId).size();

        // Calculate build success/failure trend by date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        Map<String, Map<String, Integer>> trendMap = new TreeMap<>(); // sorted by date string

        for (Build b : builds) {
            if (b.getStartTime() == null) continue;
            String dateStr = b.getStartTime().format(formatter);
            trendMap.putIfAbsent(dateStr, new HashMap<>(Map.of("SUCCESS", 0, "FAILED", 0)));
            Map<String, Integer> counts = trendMap.get(dateStr);
            String status = b.getStatus().toUpperCase();
            if (status.equalsIgnoreCase("SUCCESS")) {
                counts.put("SUCCESS", counts.get("SUCCESS") + 1);
            } else if (status.equalsIgnoreCase("FAILED")) {
                counts.put("FAILED", counts.get("FAILED") + 1);
            }
        }

        List<Map<String, Object>> buildTrends = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> entry : trendMap.entrySet()) {
            buildTrends.add(Map.of(
                    "date", entry.getKey(),
                    "success", entry.getValue().get("SUCCESS"),
                    "failed", entry.getValue().get("FAILED")
            ));
        }

        // Environment current state mapping
        List<Map<String, Object>> envStatusList = new ArrayList<>();
        List<Environment> environments = environmentRepository.findAll();
        for (Environment env : environments) {
            Optional<Deployment> latestDeploy = deployments.stream()
                    .filter(d -> d.getEnvironment().getId().equalsIgnoreCase(env.getId()))
                    .findFirst();

            String version = latestDeploy.map(Deployment::getVersion).orElse("None");
            String lastStatus = latestDeploy.map(Deployment::getStatus).orElse("NO_DEPLOYMENTS");

            envStatusList.add(Map.of(
                    "envId", env.getId(),
                    "envName", env.getName(),
                    "enabled", env.isEnabled(),
                    "activeVersion", version,
                    "lastDeployStatus", lastStatus
            ));
        }

        return new DevOpsMetricsResponse(
                totalBuilds,
                successfulBuilds,
                failedBuilds,
                runningBuilds,
                buildSuccessRate,
                averageBuildDuration,
                totalDeployments,
                successfulDeployments,
                deploymentSuccessRate,
                totalReleases,
                buildTrends,
                envStatusList
        );
    }
}
