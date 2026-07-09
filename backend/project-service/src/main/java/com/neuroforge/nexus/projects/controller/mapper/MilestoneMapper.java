package com.neuroforge.nexus.projects.controller.mapper;

import com.neuroforge.nexus.projects.domain.Milestone;
import com.neuroforge.nexus.projects.dto.MilestoneResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MilestoneMapper {

    @Mapping(target = "projectId", source = "project.id")
    MilestoneResponse toResponse(Milestone milestone);
}
