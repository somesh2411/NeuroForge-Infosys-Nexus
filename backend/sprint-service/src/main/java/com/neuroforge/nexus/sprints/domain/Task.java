package com.neuroforge.nexus.sprints.domain;

import com.neuroforge.nexus.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
public class Task extends BaseEntity {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 250)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_developer_id")
    private User assignedDeveloper;

    @Column(nullable = false, length = 50)
    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(nullable = false, length = 50)
    private String status = "TO_DO"; // TO_DO, IN_PROGRESS, CODE_REVIEW, TESTING, DONE

    @Column(name = "story_points", nullable = false)
    private int storyPoints = 1;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(length = 500)
    private String labels;

    @Column(name = "estimated_hours")
    private Double estimatedHours = 0.0;

    @Column(name = "actual_hours")
    private Double actualHours = 0.0;

    @Version
    private int version = 0;
}
