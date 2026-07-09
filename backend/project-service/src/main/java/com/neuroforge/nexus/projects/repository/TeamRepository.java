package com.neuroforge.nexus.projects.repository;

import com.neuroforge.nexus.projects.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, String> {
}
