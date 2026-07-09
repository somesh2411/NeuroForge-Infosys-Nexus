package com.neuroforge.nexus.sprints.repository;

import com.neuroforge.nexus.sprints.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, String>, JpaSpecificationExecutor<Task> {

    List<Task> findBySprintId(String sprintId);

    List<Task> findByProjectId(String projectId);

    List<Task> findByAssignedDeveloperId(String assignedDeveloperId);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.sprint IS NULL")
    List<Task> findBacklogTasksByProjectId(@Param("projectId") String projectId);
}
