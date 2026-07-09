package com.neuroforge.nexus.users.controller;

import com.neuroforge.nexus.shared.util.SecurityUtils;
import com.neuroforge.nexus.users.dto.UserRegisterRequest;
import com.neuroforge.nexus.users.dto.UserResponse;
import com.neuroforge.nexus.users.dto.UserSyncRequest;
import com.neuroforge.nexus.users.dto.UserUpdateRequest;
import com.neuroforge.nexus.users.service.UserService;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for managing user accounts, profiles, and team bindings")
public class UserController {

    private final UserService userService;

    @PostMapping("/sync")
    @Operation(summary = "Synchronize Keycloak user login profile with local service database")
    public ResponseEntity<UserResponse> syncUser(@Valid @RequestBody UserSyncRequest request) {
        log.info("REST request to sync user: {}", request.username());
        return ResponseEntity.ok(userService.syncUser(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Get the profile of the current authenticated user")
    public ResponseEntity<UserResponse> getCurrentUser() {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("REST request to get current user details: {}", userId);
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user profile by identifier")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") String id) {
        log.info("REST request to get user: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Get user profile by username")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable("username") String username) {
        log.info("REST request to get user by username: {}", username);
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @GetMapping
    @Operation(summary = "Retrieve a list of all active user profiles")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("REST request to get all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{id}/team")
    @Operation(summary = "Associate a user with a primary team")
    public ResponseEntity<UserResponse> updatePrimaryTeam(
            @PathVariable("id") String id,
            @RequestParam("teamId") String teamId) {
        log.info("REST request to update primary team of user {} to team {}", id, teamId);
        return ResponseEntity.ok(userService.updatePrimaryTeam(id, teamId));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user in Keycloak and local service database")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegisterRequest request) {
        log.info("REST request to register user: {}", request.username());
        return new ResponseEntity<>(userService.registerUser(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing user's details and role (Admins only)")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable("id") String id,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("REST request to update user details for ID: {}", id);
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete / archive a user account (Admins only)")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") String id) {
        log.info("REST request to delete user ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
