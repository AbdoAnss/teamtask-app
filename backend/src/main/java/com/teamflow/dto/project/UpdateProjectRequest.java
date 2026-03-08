package com.teamflow.dto.project;

import com.teamflow.domain.ProjectStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProjectRequest {
    @Size(max = 255)
    private String name;
    private String description;
    private ProjectStatus status;
}
