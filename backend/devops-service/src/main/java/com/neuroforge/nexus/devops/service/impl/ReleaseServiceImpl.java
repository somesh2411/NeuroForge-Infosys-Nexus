package com.neuroforge.nexus.devops.service.impl;

import com.neuroforge.nexus.shared.exception.ResourceNotFoundException;
import com.neuroforge.nexus.shared.util.SecurityUtils;
import com.neuroforge.nexus.devops.domain.*;
import com.neuroforge.nexus.devops.dto.ReleaseRequest;
import com.neuroforge.nexus.devops.dto.ReleaseResponse;
import com.neuroforge.nexus.devops.repository.BuildRepository;
import com.neuroforge.nexus.devops.repository.ReleaseRepository;
import com.neuroforge.nexus.devops.service.AuditEventService;
import com.neuroforge.nexus.devops.service.ReleaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReleaseServiceImpl implements ReleaseService {

    private static final Logger log = LoggerFactory.getLogger(ReleaseServiceImpl.class);
    private final ReleaseRepository releaseRepository;
    private final BuildRepository buildRepository;
    private final AuditEventService auditEventService;

    public ReleaseServiceImpl(ReleaseRepository releaseRepository, 
                              BuildRepository buildRepository, 
                              AuditEventService auditEventService) {
        this.releaseRepository = releaseRepository;
        this.buildRepository = buildRepository;
        this.auditEventService = auditEventService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReleaseResponse> getReleasesByProject(String projectId) {
        return releaseRepository.findByBuildPipelineProjectIdOrderByReleasedAtDesc(projectId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReleaseResponse getReleaseById(String id) {
        return releaseRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Release registry not found: " + id));
    }

    @Override
    @Transactional
    public ReleaseResponse createRelease(ReleaseRequest request) {
        log.info("Creating release version: {}", request.version());
        Build build = buildRepository.findById(request.buildId())
                .orElseThrow(() -> new ResourceNotFoundException("Build not found: " + request.buildId()));

        String actor = "SYSTEM";
        try {
            String currentUser = SecurityUtils.getCurrentUsername();
            if (currentUser != null && !currentUser.isBlank()) {
                actor = currentUser;
            }
        } catch (Exception e) {
            log.debug("Unauthenticated context. Defaulting to SYSTEM.");
        }

        LocalDateTime releasedAt = null;
        String releasedBy = null;
        if (request.status().equalsIgnoreCase("RELEASED")) {
            releasedAt = LocalDateTime.now();
            releasedBy = actor;
        }

        Release release = Release.builder()
                .id("release-" + UUID.randomUUID().toString().substring(0, 8))
                .version(request.version())
                .name(request.name())
                .releaseNotes(request.releaseNotes())
                .build(build)
                .status(request.status().toUpperCase())
                .releasedAt(releasedAt)
                .releasedBy(releasedBy)
                .build();

        // Check if version is duplicate
        if (releaseRepository.findByVersion(request.version()).isPresent()) {
            throw new IllegalArgumentException("Release version " + request.version() + " already exists.");
        }

        Release saved = releaseRepository.save(release);
        auditEventService.logEvent("RELEASE_PUBLISHED", 
                "Release version '" + request.version() + "' (" + request.name() + ") draft established with status: " + request.status());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public ReleaseResponse updateRelease(String id, ReleaseRequest request) {
        log.info("Updating release version: {}", id);
        Release release = releaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Release registry not found: " + id));

        release.setName(request.name());
        release.setReleaseNotes(request.releaseNotes());
        
        String oldStatus = release.getStatus();
        String newStatus = request.status().toUpperCase();
        release.setStatus(newStatus);

        if (newStatus.equalsIgnoreCase("RELEASED") && !oldStatus.equalsIgnoreCase("RELEASED")) {
            String actor = "SYSTEM";
            try {
                String currentUser = SecurityUtils.getCurrentUsername();
                if (currentUser != null && !currentUser.isBlank()) {
                    actor = currentUser;
                }
            } catch (Exception e) {
                log.debug("Unauthenticated context. Defaulting to SYSTEM.");
            }
            release.setReleasedAt(LocalDateTime.now());
            release.setReleasedBy(actor);
            auditEventService.logEvent("RELEASE_PUBLISHED", "Release version '" + release.getVersion() + "' officially PUBLISHED.");
        }

        Release saved = releaseRepository.save(release);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteRelease(String id) {
        log.info("Deleting release version: {}", id);
        Release release = releaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Release registry not found: " + id));

        releaseRepository.delete(release);
        auditEventService.logEvent("RELEASE_DELETED", "Release version '" + release.getVersion() + "' deleted.");
    }

    private ReleaseResponse toResponse(Release r) {
        return new ReleaseResponse(
                r.getId(),
                r.getVersion(),
                r.getName(),
                r.getReleaseNotes(),
                r.getBuild().getId(),
                r.getBuild().getBuildNumber(),
                r.getBuild().getPipeline().getName(),
                r.getStatus(),
                r.getReleasedBy(),
                r.getReleasedAt(),
                r.getCreatedAt()
        );
    }
}
