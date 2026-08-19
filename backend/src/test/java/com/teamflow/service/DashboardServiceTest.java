package com.teamflow.service;

import com.teamflow.domain.Project;
import com.teamflow.domain.TaskStatus;
import com.teamflow.domain.User;
import com.teamflow.exception.AccessDeniedException;
import com.teamflow.exception.ResourceNotFoundException;
import com.teamflow.repository.ProjectRepository;
import com.teamflow.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private TaskRepository taskRepository;

    @InjectMocks private DashboardService dashboardService;

    private Project projectWith(User owner, User... members) {
        Project project = Project.builder()
            .id(UUID.randomUUID())
            .name("Proj")
            .owner(owner)
            .build();
        project.getMembers().add(owner);
        project.getMembers().addAll(List.of(members));
        return project;
    }

    @Test
    void dashboardShouldAggregateStatusAndAssigneeCounts() {
        User owner = User.builder().id(UUID.randomUUID()).username("alice")
            .firstName("Alice").lastName("Liddell").build();
        User ghost = User.builder().id(UUID.randomUUID()).username("ghost").build();
        Project project = projectWith(owner, ghost);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.countByStatusForProject(project.getId()))
            .thenReturn(List.<Object[]>of(new Object[]{TaskStatus.TODO, 2L}, new Object[]{TaskStatus.DONE, 1L}));
        when(taskRepository.countByAssigneeForProject(project.getId()))
            .thenReturn(List.<Object[]>of(new Object[]{owner.getId(), 3L}));

        var dto = dashboardService.getProjectDashboard(project.getId(), owner.getId());

        assertEquals(3, dto.getTotalTasks());
        assertEquals(2L, dto.getTasksByStatus().get(TaskStatus.TODO));
        assertEquals(1L, dto.getTasksByStatus().get(TaskStatus.DONE));
        assertEquals(0L, dto.getTasksByStatus().get(TaskStatus.IN_PROGRESS));
        assertEquals(3L, dto.getTasksByAssignee().get(owner.getId()));
        assertEquals(3L, dto.getTasksByAssigneeName().get("Alice Liddell"));
        assertEquals(33.33, dto.getCompletionPercentage());
    }

    @Test
    void dashboardShouldUseUsernameForMemberWithoutFirstName() {
        User owner = User.builder().id(UUID.randomUUID()).username("owner").firstName("Owner").build();
        User shy = User.builder().id(UUID.randomUUID()).username("shy").build();
        Project project = projectWith(owner, shy);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.countByStatusForProject(project.getId())).thenReturn(List.of());
        when(taskRepository.countByAssigneeForProject(project.getId()))
            .thenReturn(List.<Object[]>of(new Object[]{shy.getId(), 5L}));

        var dto = dashboardService.getProjectDashboard(project.getId(), owner.getId());

        assertEquals(5L, dto.getTasksByAssigneeName().get("shy"));
        assertEquals(0, dto.getTotalTasks());
        assertEquals(0.0, dto.getCompletionPercentage());
    }

    @Test
    void dashboardShouldFailForOutsider() {
        User owner = User.builder().id(UUID.randomUUID()).username("owner").build();
        User outsider = User.builder().id(UUID.randomUUID()).username("outsider").build();
        Project project = projectWith(owner);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        assertThrows(AccessDeniedException.class, () ->
            dashboardService.getProjectDashboard(project.getId(), outsider.getId())
        );
    }

    @Test
    void dashboardShouldFailWhenProjectMissing() {
        UUID id = UUID.randomUUID();
        when(projectRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> dashboardService.getProjectDashboard(id, UUID.randomUUID()));
    }
}
