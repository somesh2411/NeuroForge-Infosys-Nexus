package com.neuroforge.nexus.devops.service;

import com.neuroforge.nexus.devops.dto.ReleaseRequest;
import com.neuroforge.nexus.devops.dto.ReleaseResponse;
import java.util.List;

public interface ReleaseService {
    List<ReleaseResponse> getReleasesByProject(String projectId);
    ReleaseResponse getReleaseById(String id);
    ReleaseResponse createRelease(ReleaseRequest request);
    ReleaseResponse updateRelease(String id, ReleaseRequest request);
    void deleteRelease(String id);
}
