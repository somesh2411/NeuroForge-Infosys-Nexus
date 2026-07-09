package com.neuroforge.nexus.users.controller.mapper;

import com.neuroforge.nexus.users.domain.User;
import com.neuroforge.nexus.users.dto.UserResponse;
import com.neuroforge.nexus.users.dto.UserSyncRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "primaryTeamId", source = "primaryTeam.id")
    @Mapping(target = "primaryTeamName", source = "primaryTeam.name")
    @Mapping(target = "role", ignore = true)
    UserResponse toResponse(User user);

    @Mapping(target = "primaryTeam", ignore = true)
    User toEntity(UserSyncRequest request);
}
