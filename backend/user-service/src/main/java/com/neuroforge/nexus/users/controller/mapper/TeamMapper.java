package com.neuroforge.nexus.users.controller.mapper;

import com.neuroforge.nexus.users.domain.Team;
import com.neuroforge.nexus.users.domain.User;
import com.neuroforge.nexus.users.dto.TeamResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TeamMapper {

    @Mapping(target = "leadId", source = "lead.id")
    @Mapping(target = "leadName", expression = "java(team.getLead() != null ? (team.getLead().getFirstName() != null ? team.getLead().getFirstName() + \" \" + team.getLead().getLastName() : team.getLead().getUsername()) : null)")
    @Mapping(target = "memberIds", source = "members", qualifiedByName = "mapMembers")
    TeamResponse toResponse(Team team);

    @Named("mapMembers")
    default List<String> mapMembers(List<User> members) {
        if (members == null) {
            return Collections.emptyList();
        }
        return members.stream()
                .map(User::getId)
                .collect(Collectors.toList());
    }
}
