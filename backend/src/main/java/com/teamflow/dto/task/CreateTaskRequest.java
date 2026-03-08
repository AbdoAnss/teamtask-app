package com.teamflow.dto.task;

import com.teamflow.domain.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateTaskRequest {
    @NotBlank @Size(max = 255)
    private String title;
    private String description;
    private TaskPriority priority = TaskPriority.MEDIUM;
    private UUID assigneeId;
    private LocalDate dueDate;
}
