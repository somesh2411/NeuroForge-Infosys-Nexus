package com.neuroforge.nexus.sprints.service;

import com.neuroforge.nexus.sprints.dto.CommentRequest;
import com.neuroforge.nexus.sprints.dto.CommentResponse;

import java.util.List;

public interface CommentService {
    CommentResponse addComment(String taskId, CommentRequest request);
    void deleteComment(String commentId);
    List<CommentResponse> getCommentsByTask(String taskId);
}
