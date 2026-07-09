package com.neuroforge.nexus.users.controller;

import com.neuroforge.nexus.users.dto.TeamRequest;
import com.neuroforge.nexus.users.dto.TeamResponse;
import com.neuroforge.nexus.users.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@Tag(name = "Team Management", description = "Endpoints for team configuration, deletion, and membership management")
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Create a new team")
    public ResponseEntity<TeamResponse> createTeam(@Valid @RequestBody TeamRequest request) {
        log.info("REST request to create team: {}", request.name());
        return new ResponseEntity<>(teamService.createTeam(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Update an existing team's details")
    public ResponseEntity<TeamResponse> updateTeam(
            @PathVariable("id") String id,
            @Valid @RequestBody TeamRequest request) {
        log.info("REST request to update team: {}", id);
        return ResponseEntity.ok(teamService.updateTeam(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get team details by ID")
    public ResponseEntity<TeamResponse> getTeamById(@PathVariable("id") String id) {
        log.info("REST request to get team by id: {}", id);
        return ResponseEntity.ok(teamService.getTeamById(id));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get team details by team Code")
    public ResponseEntity<TeamResponse> getTeamByCode(@PathVariable("code") String code) {
        log.info("REST request to get team by code: {}", code);
        return ResponseEntity.ok(teamService.getTeamByCode(code));
    }

    @GetMapping
    @Operation(summary = "Retrieve a list of all active teams")
    public ResponseEntity<List<TeamResponse>> getAllTeams() {
        log.info("REST request to get all teams");
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER')")
    @Operation(summary = "Soft delete a team (Admins and Owners only)")
    public ResponseEntity<Void> deleteTeam(@PathVariable("id") String id) {
        log.info("REST request to delete team: {}", id);
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Add a user to a team")
    public ResponseEntity<TeamResponse> addMember(
            @PathVariable("id") String id,
            @RequestParam("userId") String userId) {
        log.info("REST request to add user {} to team {}", userId, id);
        return ResponseEntity.ok(teamService.addMember(id, userId));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZATION_OWNER', 'TEAM_LEAD')")
    @Operation(summary = "Remove a user from a team")
    public ResponseEntity<TeamResponse> removeMember(
            @PathVariable("id") String id,
            @PathVariable("userId") String userId) {
        log.info("REST request to remove user {} from team {}", userId, id);
        return ResponseEntity.ok(teamService.removeMember(id, userId));
    }
}
