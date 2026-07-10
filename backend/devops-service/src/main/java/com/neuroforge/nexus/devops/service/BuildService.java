package com.neuroforge.nexus.devops.service;

import com.neuroforge.nexus.devops.dto.BuildCompareResponse;
import com.neuroforge.nexus.devops.dto.BuildResponse;
import com.neuroforge.nexus.devops.dto.PipelineStageResponse;
import java.util.List;

public interface BuildService {
    List<BuildResponse> getBuildsByPipeline(String pipelineId);
    BuildResponse getBuildById(String id);
    BuildResponse triggerBuild(String pipelineId, String triggerType);
    List<PipelineStageResponse> getBuildStages(String buildId);
    BuildCompareResponse compareBuilds(String buildIdA, String buildIdB);
    List<BuildResponse> getRecentProjectBuilds(String projectId);
}
