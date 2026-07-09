package com.neuroforge.nexus.users.service;

import com.neuroforge.nexus.users.dto.TeamRequest;
import com.neuroforge.nexus.users.dto.TeamResponse;

import java.util.List;

public interface TeamService {
    TeamResponse createTeam(TeamRequest request);
    TeamResponse updateTeam(String id, TeamRequest request);
    TeamResponse getTeamById(String id);
    TeamResponse getTeamByCode(String code);
    List<TeamResponse> getAllTeams();
    void deleteTeam(String id);
    TeamResponse addMember(String teamId, String userId);
    TeamResponse removeMember(String teamId, String userId);
}
