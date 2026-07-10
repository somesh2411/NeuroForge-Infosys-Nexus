package com.neuroforge.nexus.devops.repository;

import com.neuroforge.nexus.devops.domain.Deployment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeploymentRepository extends JpaRepository<Deployment, String> {
    List<Deployment> findByEnvironmentIdOrderByDeployedAtDesc(String environmentId);
    List<Deployment> findByBuildPipelineProjectIdOrderByDeployedAtDesc(String projectId);
    Optional<Deployment> findFirstByEnvironmentIdAndStatusOrderByDeployedAtDesc(String environmentId, String status);
}
