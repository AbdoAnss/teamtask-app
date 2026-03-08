package com.teamflow.service;

import com.teamflow.domain.*;
import com.teamflow.dto.task.CreateTaskRequest;
import com.teamflow.exception.AccessDeniedException;
import com.teamflow.repository.ProjectRepository;
import com.teamflow.repository.TaskRepository;
import com.teamflow.repository.UserRepository;
import com.teamflow.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserService userService;

    @InjectMocks private TaskService taskService;

    @Test
    void createTaskShouldFailWhenRequesterNotMember() {
        User owner = User.builder().id(UUID.randomUUID()).username("owner").build();
        User outsider = User.builder().id(UUID.randomUUID()).username("outsider").build();

        Project project = Project.builder()
            .id(UUID.randomUUID())
            .name("X")
            .owner(owner)
            .members(Set.of(owner))
            .build();

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Task A");
        request.setPriority(TaskPriority.MEDIUM);

        assertThrows(AccessDeniedException.class, () ->
            taskService.createTask(project.getId(), request, new UserPrincipal(outsider))
        );
    }
}
