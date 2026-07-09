package com.neuroforge.nexus.sprints.controller.mapper;

import com.neuroforge.nexus.sprints.domain.Sprint;
import com.neuroforge.nexus.sprints.dto.SprintResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SprintMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    SprintResponse toResponse(Sprint sprint);
}
