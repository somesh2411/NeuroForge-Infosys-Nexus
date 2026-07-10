package com.neuroforge.nexus.devops.repository;

import com.neuroforge.nexus.devops.domain.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, String> {
    List<Environment> findByEnabledTrue();
}
