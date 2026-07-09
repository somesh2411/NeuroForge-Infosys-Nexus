package com.neuroforge.nexus.sprints.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
@Getter
@Setter
@NoArgsConstructor
public class ActivityLog {

    @Id
    @Column(length = 50)
    private String id;

    @Column(name = "task_id", length = 50)
    private String taskId;

    @Column(name = "sprint_id", length = 50)
    private String sprintId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false, length = 100)
    private String actor;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}
