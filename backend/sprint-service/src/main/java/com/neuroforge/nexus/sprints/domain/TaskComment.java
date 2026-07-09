package com.neuroforge.nexus.sprints.domain;

import com.neuroforge.nexus.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "task_comments")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
public class TaskComment extends BaseEntity {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "author_username", nullable = false, length = 100)
    private String authorUsername;
}
