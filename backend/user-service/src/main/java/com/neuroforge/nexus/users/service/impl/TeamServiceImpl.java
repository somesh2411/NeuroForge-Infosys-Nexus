package com.neuroforge.nexus.users.service.impl;

import com.neuroforge.nexus.shared.exception.ConflictException;
import com.neuroforge.nexus.shared.exception.ResourceNotFoundException;
import com.neuroforge.nexus.shared.util.SecurityUtils;
import com.neuroforge.nexus.users.controller.mapper.TeamMapper;
import com.neuroforge.nexus.users.domain.Team;
import com.neuroforge.nexus.users.domain.User;
import com.neuroforge.nexus.users.dto.TeamRequest;
import com.neuroforge.nexus.users.dto.TeamResponse;
import com.neuroforge.nexus.users.event.TeamCreatedEvent;
import com.neuroforge.nexus.users.event.TeamDeletedEvent;
import com.neuroforge.nexus.users.event.UserEventPublisher;
import com.neuroforge.nexus.users.repository.TeamRepository;
import com.neuroforge.nexus.users.repository.UserRepository;
import com.neuroforge.nexus.users.service.TeamService;
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
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamMapper teamMapper;
    private final UserEventPublisher eventPublisher;

    @Override
    @Transactional
    public TeamResponse createTeam(TeamRequest request) {
        log.info("Creating team: {}", request.name());
        
        if (teamRepository.existsByName(request.name())) {
            throw new ConflictException("Team name already exists: " + request.name());
        }
        
        String codeUpper = request.code().toUpperCase();
        if (teamRepository.existsByCode(codeUpper)) {
            throw new ConflictException("Team code already exists: " + codeUpper);
        }

        Team team = new Team();
        team.setId(UUID.randomUUID().toString());
        team.setName(request.name());
        team.setCode(codeUpper);
        team.setDescription(request.description());

        if (request.leadId() != null && !request.leadId().isBlank()) {
            User lead = userRepository.findById(request.leadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lead user not found with id: " + request.leadId()));
            team.setLead(lead);
            lead.setPrimaryTeam(team);
            userRepository.save(lead);
        }

        Team saved = teamRepository.save(team);

        // Publish event to Kafka
        eventPublisher.publishTeamCreated(new TeamCreatedEvent(
                saved.getId(),
                saved.getName(),
                saved.getCode(),
                saved.getDescription(),
                saved.getLead() != null ? saved.getLead().getId() : null
        ));

        return teamMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TeamResponse updateTeam(String id, TeamRequest request) {
        log.info("Updating team: {}", id);
        
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));

        if (!team.getName().equals(request.name()) && teamRepository.existsByName(request.name())) {
            throw new ConflictException("Team name already exists: " + request.name());
        }
        
        String codeUpper = request.code().toUpperCase();
        if (!team.getCode().equals(codeUpper) && teamRepository.existsByCode(codeUpper)) {
            throw new ConflictException("Team code already exists: " + codeUpper);
        }

        team.setName(request.name());
        team.setCode(codeUpper);
        team.setDescription(request.description());

        if (request.leadId() != null && !request.leadId().isBlank()) {
            User lead = userRepository.findById(request.leadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lead user not found with id: " + request.leadId()));
            team.setLead(lead);
            lead.setPrimaryTeam(team);
            userRepository.save(lead);
        } else {
            team.setLead(null);
        }

        Team saved = teamRepository.save(team);
        return teamMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse getTeamById(String id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
        return teamMapper.toResponse(team);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse getTeamByCode(String code) {
        Team team = teamRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with code: " + code));
        return teamMapper.toResponse(team);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(teamMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteTeam(String id) {
        log.info("Deleting team: {}", id);
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));

        // Soft delete
        String currentUserId = SecurityUtils.getCurrentUserId();
        team.softDelete(currentUserId);
        
        // Remove primary team relationship for all members
        for (User member : team.getMembers()) {
            member.setPrimaryTeam(null);
            userRepository.save(member);
        }
        team.getMembers().clear();
        
        teamRepository.save(team);

        // Publish event to Kafka
        eventPublisher.publishTeamDeleted(new TeamDeletedEvent(id));
    }

    @Override
    @Transactional
    public TeamResponse addMember(String teamId, String userId) {
        log.info("Adding user {} to team {}", userId, teamId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setPrimaryTeam(team);
        userRepository.save(user);

        // Update in-memory bi-directional collection to prevent L1 caching lag
        if (!team.getMembers().contains(user)) {
            team.getMembers().add(user);
        }

        return teamMapper.toResponse(team);
    }

    @Override
    @Transactional
    public TeamResponse removeMember(String teamId, String userId) {
        log.info("Removing user {} from team {}", userId, teamId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getPrimaryTeam() != null && user.getPrimaryTeam().getId().equals(teamId)) {
            user.setPrimaryTeam(null);
            userRepository.save(user);
            team.getMembers().remove(user);
        }

        return teamMapper.toResponse(team);
    }
}
