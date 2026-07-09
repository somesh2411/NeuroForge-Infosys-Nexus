package com.neuroforge.nexus.projects.domain;

import com.neuroforge.nexus.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "milestones")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
public class Milestone extends BaseEntity {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "target_date")
    private LocalDateTime targetDate;

    @Column(nullable = false, length = 50)
    private String status = "PLANNED"; // PLANNED, ACHIEVED, MISSED
}
