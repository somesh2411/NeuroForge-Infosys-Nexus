package com.neuroforge.nexus.devops.repository;

import com.neuroforge.nexus.devops.domain.Release;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReleaseRepository extends JpaRepository<Release, String> {
    List<Release> findByBuildPipelineProjectIdOrderByReleasedAtDesc(String projectId);
    Optional<Release> findByVersion(String version);
}
