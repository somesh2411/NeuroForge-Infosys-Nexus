package com.neuroforge.nexus.users.repository;

import com.neuroforge.nexus.users.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, String> {
    Optional<Team> findByCode(String code);
    boolean existsByName(String name);
    boolean existsByCode(String code);
}
