package com.neuroforge.nexus.devops.dto;

import java.time.LocalDateTime;

public record PipelineResponse(
    String id,
    String name,
    String projectId,
    String repositoryId,
    String repositoryName,
    String repositoryUrl,
    String branch,
    String buildTool,
    String pipelineType,
    String pipelineTemplate,
    String jenkinsJobName,
    String githubWorkflowPath,
    boolean enabled,
    String status,
    LocalDateTime createdAt,
    String createdBy
) {}
