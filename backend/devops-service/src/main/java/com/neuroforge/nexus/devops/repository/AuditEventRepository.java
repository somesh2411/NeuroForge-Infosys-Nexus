package com.neuroforge.nexus.devops.repository;

import com.neuroforge.nexus.devops.domain.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, String> {
    List<AuditEvent> findTop100ByOrderByTimestampDesc();
}
