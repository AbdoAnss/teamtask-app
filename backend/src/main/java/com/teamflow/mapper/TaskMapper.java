package com.teamflow.mapper;

import com.teamflow.domain.Task;
import com.teamflow.dto.task.TaskDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface TaskMapper {

    @Mapping(target = "projectId", source = "task.project.id")
    @Mapping(target = "projectName", source = "task.project.name")
    TaskDto toDto(Task task);
}
