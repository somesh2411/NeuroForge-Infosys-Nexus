package com.neuroforge.nexus.sprints.repository;

import com.neuroforge.nexus.sprints.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {
}
