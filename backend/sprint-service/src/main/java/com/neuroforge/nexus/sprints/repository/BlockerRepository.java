package com.neuroforge.nexus.sprints.repository;

import com.neuroforge.nexus.sprints.domain.Blocker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockerRepository extends JpaRepository<Blocker, String> {
    List<Blocker> findByTaskId(String taskId);
}
