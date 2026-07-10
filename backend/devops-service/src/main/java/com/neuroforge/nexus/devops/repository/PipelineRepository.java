package com.neuroforge.nexus.devops.repository;

import com.neuroforge.nexus.devops.domain.Pipeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PipelineRepository extends JpaRepository<Pipeline, String> {
    List<Pipeline> findByProjectId(String projectId);
}
