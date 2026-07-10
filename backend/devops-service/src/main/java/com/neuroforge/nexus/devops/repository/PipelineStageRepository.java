package com.neuroforge.nexus.devops.repository;

import com.neuroforge.nexus.devops.domain.PipelineStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PipelineStageRepository extends JpaRepository<PipelineStage, String> {
    List<PipelineStage> findByBuildId(String buildId);
}
