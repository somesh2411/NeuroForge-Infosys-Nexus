package com.neuroforge.nexus.projects.service;

import com.neuroforge.nexus.projects.dto.MilestoneRequest;
import com.neuroforge.nexus.projects.dto.MilestoneResponse;

import java.util.List;

public interface MilestoneService {
    MilestoneResponse createMilestone(String projectId, MilestoneRequest request);
    MilestoneResponse updateMilestone(String id, MilestoneRequest request);
    MilestoneResponse getMilestoneById(String id);
    List<MilestoneResponse> getMilestonesByProject(String projectId);
    void deleteMilestone(String id);
}
