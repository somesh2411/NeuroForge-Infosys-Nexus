package com.neuroforge.nexus.projects.controller.mapper;

import com.neuroforge.nexus.projects.domain.Project;
import com.neuroforge.nexus.projects.domain.Team;
import com.neuroforge.nexus.projects.dto.ProjectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "managerId", source = "manager.id")
    @Mapping(target = "managerName", expression = "java(project.getManager() != null ? (project.getManager().getFirstName() != null ? project.getManager().getFirstName() + \" \" + project.getManager().getLastName() : project.getManager().getUsername()) : null)")
    @Mapping(target = "teamIds", source = "teams", qualifiedByName = "mapTeams")
    ProjectResponse toResponse(Project project);

    @Named("mapTeams")
    default List<String> mapTeams(List<Team> teams) {
        if (teams == null) {
            return Collections.emptyList();
        }
        return teams.stream()
                .map(Team::getId)
                .collect(Collectors.toList());
    }
}
