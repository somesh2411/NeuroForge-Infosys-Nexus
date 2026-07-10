package com.neuroforge.nexus.devops.repository;

import com.neuroforge.nexus.devops.domain.Build;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuildRepository extends JpaRepository<Build, String> {
    List<Build> findByPipelineIdOrderByBuildNumberDesc(String pipelineId);
    Optional<Build> findFirstByPipelineIdOrderByBuildNumberDesc(String pipelineId);
    List<Build> findTop10ByPipelineProjectIdOrderByCreatedAtDesc(String projectId);
}
