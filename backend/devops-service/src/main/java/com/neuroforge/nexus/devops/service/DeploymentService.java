package com.neuroforge.nexus.devops.service;

import com.neuroforge.nexus.devops.dto.DeploymentRequest;
import com.neuroforge.nexus.devops.dto.DeploymentResponse;
import java.util.List;

public interface DeploymentService {
    List<DeploymentResponse> getDeploymentsByEnvironment(String environmentId);
    List<DeploymentResponse> getProjectDeployments(String projectId);
    DeploymentResponse triggerDeployment(DeploymentRequest request);
    DeploymentResponse rollbackDeployment(String id, String rollbackReason);
    DeploymentResponse getDeploymentById(String id);
}
