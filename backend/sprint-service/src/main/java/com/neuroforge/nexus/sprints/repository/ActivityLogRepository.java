package com.neuroforge.nexus.sprints.repository;

import com.neuroforge.nexus.sprints.domain.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, String> {
    List<ActivityLog> findByTaskIdOrderByTimestampDesc(String taskId);
    List<ActivityLog> findBySprintIdOrderByTimestampDesc(String sprintId);
}
