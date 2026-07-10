package com.neuroforge.nexus.devops.provider;

import com.neuroforge.nexus.devops.domain.Build;
import com.neuroforge.nexus.devops.domain.Pipeline;

public interface PipelineProvider {
    String getProviderType(); // e.g. "JENKINS", "GITHUB_ACTIONS", "MOCK"
    void executePipeline(Pipeline pipeline, Build build) throws Exception;
    String fetchStageLogs(Pipeline pipeline, String buildId, String stageName) throws Exception;
}
