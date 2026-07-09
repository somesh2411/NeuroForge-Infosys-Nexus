package com.neuroforge.nexus.sprints.repository;

import com.neuroforge.nexus.sprints.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
}
