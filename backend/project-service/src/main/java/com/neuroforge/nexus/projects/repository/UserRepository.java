package com.neuroforge.nexus.projects.repository;

import com.neuroforge.nexus.projects.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
}
