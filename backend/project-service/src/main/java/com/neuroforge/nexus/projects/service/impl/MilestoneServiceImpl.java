package com.neuroforge.nexus.projects.service.impl;

import com.neuroforge.nexus.shared.exception.ResourceNotFoundException;
import com.neuroforge.nexus.shared.util.SecurityUtils;
import com.neuroforge.nexus.projects.controller.mapper.MilestoneMapper;
import com.neuroforge.nexus.projects.domain.Milestone;
import com.neuroforge.nexus.projects.domain.Project;
import com.neuroforge.nexus.projects.dto.MilestoneRequest;
import com.neuroforge.nexus.projects.dto.MilestoneResponse;
import com.neuroforge.nexus.projects.repository.MilestoneRepository;
import com.neuroforge.nexus.projects.repository.ProjectRepository;
import com.neuroforge.nexus.projects.service.MilestoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MilestoneServiceImpl implements MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;
    private final MilestoneMapper milestoneMapper;

    @Override
    @Transactional
    public MilestoneResponse createMilestone(String projectId, MilestoneRequest request) {
        log.info("Creating milestone for project: {}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        Milestone milestone = new Milestone();
        milestone.setId(UUID.randomUUID().toString());
        milestone.setProject(project);
        milestone.setName(request.name());
        milestone.setDescription(request.description());
        milestone.setTargetDate(request.targetDate());
        if (request.status() != null) {
            milestone.setStatus(request.status());
        }

        Milestone saved = milestoneRepository.save(milestone);
        return milestoneMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public MilestoneResponse updateMilestone(String id, MilestoneRequest request) {
        log.info("Updating milestone: {}", id);
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id: " + id));

        milestone.setName(request.name());
        milestone.setDescription(request.description());
        milestone.setTargetDate(request.targetDate());
        if (request.status() != null) {
            milestone.setStatus(request.status());
        }

        Milestone saved = milestoneRepository.save(milestone);
        return milestoneMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MilestoneResponse getMilestoneById(String id) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id: " + id));
        return milestoneMapper.toResponse(milestone);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneResponse> getMilestonesByProject(String projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        return milestoneRepository.findByProjectId(projectId).stream()
                .map(milestoneMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteMilestone(String id) {
        log.info("Deleting milestone: {}", id);
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id: " + id));

        milestone.softDelete(SecurityUtils.getCurrentUserId());
        milestoneRepository.save(milestone);
    }
}
