package com.neuroforge.nexus.projects.repository;

import com.neuroforge.nexus.projects.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {
    Optional<Project> findByKey(String key);
    boolean existsByName(String name);
    boolean existsByKey(String key);
}
