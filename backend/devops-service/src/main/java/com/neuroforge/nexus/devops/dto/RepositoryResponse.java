package com.neuroforge.nexus.devops.dto;

import java.time.LocalDateTime;

public record RepositoryResponse(
    String id,
    String name,
    String url,
    String defaultBranch,
    String lastCommitId,
    String lastCommitMessage,
    String lastCommitAuthor,
    LocalDateTime lastCommitAt,
    LocalDateTime createdAt
) {}
