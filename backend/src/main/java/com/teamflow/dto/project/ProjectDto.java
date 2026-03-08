package com.teamflow.dto.project;

import com.teamflow.domain.ProjectStatus;
import com.teamflow.dto.user.UserDto;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data @Builder
public class ProjectDto {
    private UUID id;
    private String name;
    private String description;
    private ProjectStatus status;
    private UserDto owner;
    private Set<UserDto> members;
    private int taskCount;
    private Instant createdAt;
    private Instant updatedAt;
}
