package com.neuroforge.nexus.devops.service;

import com.neuroforge.nexus.devops.dto.EnvironmentRequest;
import com.neuroforge.nexus.devops.dto.EnvironmentResponse;
import java.util.List;

public interface EnvironmentService {
    List<EnvironmentResponse> getAllEnvironments();
    EnvironmentResponse getEnvironmentById(String id);
    EnvironmentResponse createEnvironment(EnvironmentRequest request);
    EnvironmentResponse updateEnvironment(String id, EnvironmentRequest request);
    void deleteEnvironment(String id);
    EnvironmentResponse toggleEnvironmentStatus(String id, boolean enabled);
}
