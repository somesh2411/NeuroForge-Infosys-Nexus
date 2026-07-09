package com.neuroforge.nexus.sprints.service;

import com.neuroforge.nexus.sprints.dto.BlockerRequest;
import com.neuroforge.nexus.sprints.dto.BlockerResponse;

import java.util.List;

public interface BlockerService {
    BlockerResponse addBlocker(String taskId, BlockerRequest request);
    BlockerResponse resolveBlocker(String blockerId);
    void deleteBlocker(String blockerId);
    List<BlockerResponse> getBlockersByTask(String taskId);
}
