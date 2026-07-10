package com.neuroforge.nexus.devops.service;

import com.neuroforge.nexus.devops.dto.AuditEventResponse;
import java.util.List;

public interface AuditEventService {
    void logEvent(String eventType, String message);
    List<AuditEventResponse> getAuditEvents();
}
