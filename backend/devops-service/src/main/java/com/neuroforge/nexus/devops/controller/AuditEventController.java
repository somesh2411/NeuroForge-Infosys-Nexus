package com.neuroforge.nexus.devops.controller;

import com.neuroforge.nexus.devops.dto.AuditEventResponse;
import com.neuroforge.nexus.devops.service.AuditEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/devops-analytics/audit-events")
@Tag(name = "DevOps Audit Timeline", description = "Endpoints for viewing historical pipeline actions and deployments audit events logs")
public class AuditEventController {

    private static final Logger log = LoggerFactory.getLogger(AuditEventController.class);
    private final AuditEventService auditEventService;

    public AuditEventController(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @GetMapping
    @Operation(summary = "Get top 100 historical audit events")
    public ResponseEntity<List<AuditEventResponse>> getAuditEvents() {
        log.info("REST request to fetch audit logs timeline");
        return ResponseEntity.ok(auditEventService.getAuditEvents());
    }
}
