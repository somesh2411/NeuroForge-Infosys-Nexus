package com.neuroforge.nexus.users.service.impl;

import com.neuroforge.nexus.shared.exception.ResourceNotFoundException;
import com.neuroforge.nexus.users.controller.mapper.UserMapper;
import com.neuroforge.nexus.users.domain.Team;
import com.neuroforge.nexus.users.domain.User;
import com.neuroforge.nexus.users.dto.UserRegisterRequest;
import com.neuroforge.nexus.users.dto.UserResponse;
import com.neuroforge.nexus.users.dto.UserSyncRequest;
import com.neuroforge.nexus.users.dto.UserUpdateRequest;
import com.neuroforge.nexus.users.event.UserCreatedEvent;
import com.neuroforge.nexus.users.event.UserEventPublisher;
import com.neuroforge.nexus.users.event.UserUpdatedEvent;
import com.neuroforge.nexus.users.repository.TeamRepository;
import com.neuroforge.nexus.users.repository.UserRepository;
import com.neuroforge.nexus.users.service.KeycloakAdminService;
import com.neuroforge.nexus.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final UserMapper userMapper;
    private final UserEventPublisher eventPublisher;
    private final KeycloakAdminService keycloakAdminService;

    private UserResponse mapToResponse(User user) {
        UserResponse base = userMapper.toResponse(user);
        String role = keycloakAdminService.getUserRole(user.getId());
        return new UserResponse(
                base.id(),
                base.username(),
                base.email(),
                base.firstName(),
                base.lastName(),
                base.primaryTeamId(),
                base.primaryTeamName(),
                role,
                base.createdAt()
        );
    }

    @Override
    @Transactional
    public UserResponse syncUser(UserSyncRequest request) {
        log.info("Syncing user from Keycloak: {}", request.username());
        
        User user = userRepository.findById(request.id())
                .map(existingUser -> {
                    existingUser.setFirstName(request.firstName());
                    existingUser.setLastName(request.lastName());
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    User newUser = userMapper.toEntity(request);
                    return userRepository.save(newUser);
                });

        // Always publish event to ensure downstream read-models are in sync
        eventPublisher.publishUserCreated(new UserCreatedEvent(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        ));

        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse updatePrimaryTeam(String userId, String teamId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));
        
        user.setPrimaryTeam(team);
        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse registerUser(UserRegisterRequest request) {
        log.info("Registering user: {}", request.username());
        
        // 1. Create User in Keycloak
        String keycloakUserId = keycloakAdminService.createUser(
                request.username(),
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName()
        );
        
        // 2. Assign Role in Keycloak
        keycloakAdminService.assignRole(keycloakUserId, request.role());
        
        // 3. Save User Locally
        User user = new User();
        user.setId(keycloakUserId);
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        User saved = userRepository.save(user);
        
        // 4. Publish Event
        eventPublisher.publishUserCreated(new UserCreatedEvent(
                saved.getId(),
                saved.getUsername(),
                saved.getEmail(),
                saved.getFirstName(),
                saved.getLastName()
        ));
        
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse updateUser(String id, UserUpdateRequest request) {
        log.info("Updating user details for ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // 1. Update in Keycloak
        keycloakAdminService.updateUser(
                id,
                request.email(),
                request.firstName(),
                request.lastName()
        );
        
        // 2. Update Keycloak Role
        keycloakAdminService.updateUserRole(id, request.role());
        
        // 3. Update Locally
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        
        if (request.primaryTeamId() != null && !request.primaryTeamId().isBlank()) {
            Team team = teamRepository.findById(request.primaryTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + request.primaryTeamId()));
            user.setPrimaryTeam(team);
        } else {
            user.setPrimaryTeam(null);
        }
        
        User saved = userRepository.save(user);
        
        // 4. Publish Event to Kafka
        eventPublisher.publishUserUpdated(new UserUpdatedEvent(
                saved.getId(),
                saved.getUsername(),
                saved.getEmail(),
                saved.getFirstName(),
                saved.getLastName()
        ));
        
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        log.info("Deleting user ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // 1. Delete in Keycloak
        keycloakAdminService.deleteUser(id);
        
        // 2. Soft delete locally
        user.softDelete(com.neuroforge.nexus.shared.util.SecurityUtils.getCurrentUserId());
        userRepository.save(user);
    }
}
