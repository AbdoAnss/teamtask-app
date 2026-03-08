package com.teamflow.dto.task;

import com.teamflow.domain.TaskPriority;
import com.teamflow.domain.TaskStatus;
import com.teamflow.dto.user.UserDto;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data @Builder
public class TaskDto {
    private UUID id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private UUID projectId;
    private String projectName;
    private UserDto assignee;
    private UserDto reporter;
    private LocalDate dueDate;
    private Instant createdAt;
    private Instant updatedAt;
}
