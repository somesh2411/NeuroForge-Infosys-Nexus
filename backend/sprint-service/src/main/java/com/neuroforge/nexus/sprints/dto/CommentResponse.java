package com.neuroforge.nexus.sprints.dto;

import java.time.LocalDateTime;

public record CommentResponse(
    String id,
    String taskId,
    String content,
    String authorUsername,
    LocalDateTime createdAt
) {}
