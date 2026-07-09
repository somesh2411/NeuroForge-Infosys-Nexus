package com.neuroforge.nexus.users.service;

import com.neuroforge.nexus.users.dto.UserRegisterRequest;
import com.neuroforge.nexus.users.dto.UserResponse;
import com.neuroforge.nexus.users.dto.UserSyncRequest;
import com.neuroforge.nexus.users.dto.UserUpdateRequest;

import java.util.List;

public interface UserService {
    UserResponse syncUser(UserSyncRequest request);
    UserResponse getUserById(String id);
    UserResponse getUserByUsername(String username);
    List<UserResponse> getAllUsers();
    UserResponse updatePrimaryTeam(String userId, String teamId);
    UserResponse registerUser(UserRegisterRequest request);
    UserResponse updateUser(String id, UserUpdateRequest request);
    void deleteUser(String id);
}
