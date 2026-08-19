package com.teamflow.mapper;

import com.teamflow.domain.Project;
import com.teamflow.dto.project.ProjectDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface ProjectMapper {

    @Mapping(target = "taskCount", expression = "java(project.getTasks().size())")
    ProjectDto toDto(Project project);
}
