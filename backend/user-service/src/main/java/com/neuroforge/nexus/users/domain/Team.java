package com.neuroforge.nexus.users.domain;

import com.neuroforge.nexus.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
public class Team extends BaseEntity {

    @Id
    @Column(length = 50)
    private String id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private User lead;

    @OneToMany(mappedBy = "primaryTeam", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<User> members = new ArrayList<>();
}
