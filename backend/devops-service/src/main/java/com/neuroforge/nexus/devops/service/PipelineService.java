package com.neuroforge.nexus.devops.service;

import com.neuroforge.nexus.devops.dto.PipelineRequest;
import com.neuroforge.nexus.devops.dto.PipelineResponse;
import java.util.List;

public interface PipelineService {
    List<PipelineResponse> getPipelinesByProject(String projectId);
    PipelineResponse getPipelineById(String id);
    PipelineResponse createPipeline(PipelineRequest request);
    PipelineResponse updatePipeline(String id, PipelineRequest request);
    void deletePipeline(String id);
    PipelineResponse togglePipeline(String id, boolean enabled);
}
