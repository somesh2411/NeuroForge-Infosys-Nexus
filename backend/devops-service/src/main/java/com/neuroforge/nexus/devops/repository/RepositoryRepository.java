package com.neuroforge.nexus.devops.repository;

import com.neuroforge.nexus.devops.domain.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RepositoryRepository extends JpaRepository<Repository, String> {
    Optional<Repository> findByUrl(String url);
}
