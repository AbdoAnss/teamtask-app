package com.teamflow.service;

import com.teamflow.domain.Project;
import com.teamflow.domain.TaskStatus;
import com.teamflow.dto.dashboard.ProjectDashboardDto;
import com.teamflow.exception.AccessDeniedException;
import com.teamflow.exception.ResourceNotFoundException;
import com.teamflow.repository.ProjectRepository;
import com.teamflow.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public ProjectDashboardDto getProjectDashboard(UUID projectId, UUID requesterId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        boolean isMember = project.getMembers().stream().anyMatch(m -> m.getId().equals(requesterId));
        boolean isOwner = project.getOwner().getId().equals(requesterId);
        if (!isMember && !isOwner) {
            throw new AccessDeniedException("You are not a member of this project");
        }

        Map<TaskStatus, Long> statusMap = Arrays.stream(TaskStatus.values())
            .collect(Collectors.toMap(s -> s, s -> 0L));

        for (Object[] row : taskRepository.countByStatusForProject(projectId)) {
            statusMap.put((TaskStatus) row[0], (Long) row[1]);
        }

        Map<UUID, Long> byAssignee = new HashMap<>();
        for (Object[] row : taskRepository.countByAssigneeForProject(projectId)) {
            byAssignee.put((UUID) row[0], (Long) row[1]);
        }

        Map<String, Long> byAssigneeName = project.getMembers().stream()
            .filter(u -> byAssignee.containsKey(u.getId()))
            .collect(Collectors.toMap(
                u -> u.getFirstName() != null ? (u.getFirstName() + " " + Optional.ofNullable(u.getLastName()).orElse("")) : u.getUsername(),
                u -> byAssignee.get(u.getId())
            ));

        long total = statusMap.values().stream().mapToLong(Long::longValue).sum();
        long done = statusMap.getOrDefault(TaskStatus.DONE, 0L);
        double completion = total == 0 ? 0d : (done * 100.0) / total;

        return ProjectDashboardDto.builder()
            .projectId(project.getId())
            .projectName(project.getName())
            .totalTasks((int) total)
            .tasksByStatus(statusMap)
            .tasksByAssignee(byAssignee)
            .tasksByAssigneeName(byAssigneeName)
            .completionPercentage(Math.round(completion * 100.0) / 100.0)
            .build();
    }
}
