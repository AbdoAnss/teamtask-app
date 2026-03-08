package com.teamflow.dto.dashboard;

import com.teamflow.domain.TaskStatus;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data @Builder
public class ProjectDashboardDto {
    private UUID projectId;
    private String projectName;
    private int totalTasks;
    private Map<TaskStatus, Long> tasksByStatus;
    private Map<UUID, Long> tasksByAssignee;
    private Map<String, Long> tasksByAssigneeName;
    private double completionPercentage;
}
