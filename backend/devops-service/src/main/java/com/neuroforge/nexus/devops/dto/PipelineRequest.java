package com.neuroforge.nexus.devops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PipelineRequest(
    @NotBlank(message = "Pipeline name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    String name,

    @NotBlank(message = "Project ID is required")
    String projectId,

    String repositoryUrl,
    String repositoryName,
    String branch,

    @NotBlank(message = "Build tool is required")
    String buildTool, // MAVEN, GRADLE, NPM, DOCKER

    @NotBlank(message = "Pipeline type is required")
    String pipelineType, // MOCK, JENKINS, GITHUB_ACTIONS

    String pipelineTemplate, // JAVA_MAVEN, SPRING_BOOT, ANGULAR, DOCKER
    
    String jenkinsJobName,
    String githubWorkflowPath,
    boolean enabled
) {}
