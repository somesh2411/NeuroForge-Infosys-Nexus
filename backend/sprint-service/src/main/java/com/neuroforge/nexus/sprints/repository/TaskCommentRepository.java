package com.neuroforge.nexus.sprints.repository;

import com.neuroforge.nexus.sprints.domain.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, String> {
    List<TaskComment> findByTaskIdOrderByCreatedAtAsc(String taskId);
}
