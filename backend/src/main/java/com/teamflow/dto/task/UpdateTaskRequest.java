package com.teamflow.dto.task;

import com.teamflow.domain.TaskPriority;
import com.teamflow.domain.TaskStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UpdateTaskRequest {
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private UUID assigneeId;
    private LocalDate dueDate;
}
