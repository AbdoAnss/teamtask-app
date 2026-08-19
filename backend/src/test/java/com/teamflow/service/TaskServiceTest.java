package com.teamflow.service;

import com.teamflow.domain.*;
import com.teamflow.dto.task.CreateTaskRequest;
import com.teamflow.dto.task.UpdateTaskRequest;
import com.teamflow.exception.AccessDeniedException;
import com.teamflow.exception.ResourceNotFoundException;
import com.teamflow.mapper.TaskMapperImpl;
import com.teamflow.mapper.UserMapperImpl;
import com.teamflow.repository.ProjectRepository;
import com.teamflow.repository.TaskRepository;
import com.teamflow.repository.UserRepository;
import com.teamflow.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;

    private TaskService taskService;

    @BeforeEach
    void init() {
        TaskMapperImpl taskMapper = new TaskMapperImpl();
        ReflectionTestUtils.setField(taskMapper, "userMapper", new UserMapperImpl());
        taskService = new TaskService(taskRepository, projectRepository, userRepository, taskMapper);
    }

    private Project projectOwnedBy(User owner, User... extraMembers) {
        Project project = Project.builder()
            .id(UUID.randomUUID())
            .name("Mobile App")
            .owner(owner)
            .build();
        project.getMembers().add(owner);
        project.getMembers().addAll(List.of(extraMembers));
        return project;
    }

    @Test
    void createTaskShouldFailWhenRequesterNotMember() {
        User owner = User.builder().id(UUID.randomUUID()).username("owner").build();
        User outsider = User.builder().id(UUID.randomUUID()).username("outsider").build();
        Project project = projectOwnedBy(owner);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Task A");
        request.setPriority(TaskPriority.MEDIUM);

        assertThrows(AccessDeniedException.class, () ->
            taskService.createTask(project.getId(), request, new UserPrincipal(outsider))
        );
    }

    @Test
    void createTaskShouldMapFieldsAndAssignee() {
        User owner = User.builder().id(UUID.randomUUID()).username("owner").build();
        User assignee = User.builder().id(UUID.randomUUID()).username("assignee").email("a@t.io").build();
        Project project = projectOwnedBy(owner);
        Task saved = Task.builder()
            .id(UUID.randomUUID()).title("Write docs").description("API guide")
            .status(TaskStatus.TODO).priority(TaskPriority.HIGH)
            .project(project).assignee(assignee).reporter(owner)
            .build();

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userRepository.findById(assignee.getId())).thenReturn(Optional.of(assignee));
        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Write docs");
        request.setDescription("API guide");
        request.setPriority(TaskPriority.HIGH);
        request.setAssigneeId(assignee.getId());

        var dto = taskService.createTask(project.getId(), request, new UserPrincipal(owner));

        assertEquals("Write docs", dto.getTitle());
        assertEquals(project.getId(), dto.getProjectId());
        assertEquals("Mobile App", dto.getProjectName());
        assertEquals("assignee", dto.getAssignee().getUsername());
        assertEquals("owner", dto.getReporter().getUsername());
    }

    @Test
    void getTasksByProjectShouldNormalizeTitleFilter() {
        User owner = User.builder().id(UUID.randomUUID()).username("owner").build();
        Project project = projectOwnedBy(owner);
        Task task = Task.builder().id(UUID.randomUUID()).title("Bug fix").project(project).reporter(owner).build();

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.search(eq(project.getId()), isNull(), isNull(), eq("bug"), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(List.of(task)));

        var page = taskService.getTasksByProject(
            project.getId(), null, null, "  bug  ", owner.getId(), PageRequest.of(0, 20));

        assertEquals(1, page.getContent().size());
        assertEquals("Bug fix", page.getContent().get(0).getTitle());
    }

    @Test
    void getTaskByIdShouldFailWhenTaskMissing() {
        UUID id = UUID.randomUUID();
        when(taskRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(id, UUID.randomUUID()));
    }

    @Test
    void updateTaskShouldApplyProvidedFields() {
        User owner = User.builder().id(UUID.randomUUID()).username("owner").build();
        User newAssignee = User.builder().id(UUID.randomUUID()).username("newbie").build();
        Project project = projectOwnedBy(owner);
        Task task = Task.builder()
            .id(UUID.randomUUID()).title("Old title").status(TaskStatus.TODO).priority(TaskPriority.LOW)
            .project(project).reporter(owner).build();

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userRepository.findById(newAssignee.getId())).thenReturn(Optional.of(newAssignee));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("New title");
        request.setStatus(TaskStatus.IN_PROGRESS);
        request.setAssigneeId(newAssignee.getId());

        var dto = taskService.updateTask(task.getId(), request, new UserPrincipal(owner));

        assertEquals("New title", task.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertEquals(newAssignee, task.getAssignee());
        assertEquals("New title", dto.getTitle());
    }

    @Test
    void updateTaskShouldFailForNonMember() {
        User owner = User.builder().id(UUID.randomUUID()).username("owner").build();
        User outsider = User.builder().id(UUID.randomUUID()).username("outsider").build();
        Project project = projectOwnedBy(owner);
        Task task = Task.builder().id(UUID.randomUUID()).title("T").project(project).reporter(owner).build();

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () ->
            taskService.updateTask(task.getId(), new UpdateTaskRequest(), new UserPrincipal(outsider))
        );
    }

    @Test
    void deleteTaskShouldFailForMemberWhoIsNeitherOwnerNorReporter() {
        User owner = User.builder().id(UUID.randomUUID()).username("owner").build();
        User reporter = User.builder().id(UUID.randomUUID()).username("reporter").build();
        User plainMember = User.builder().id(UUID.randomUUID()).username("member").build();
        Project project = projectOwnedBy(owner, reporter);
        Task task = Task.builder().id(UUID.randomUUID()).title("T").project(project).reporter(reporter).build();

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () ->
            taskService.deleteTask(task.getId(), new UserPrincipal(plainMember))
        );
    }

    @Test
    void deleteTaskShouldAllowReporter() {
        User owner = User.builder().id(UUID.randomUUID()).username("owner").build();
        Project project = projectOwnedBy(owner);
        Task task = Task.builder().id(UUID.randomUUID()).title("T").project(project).reporter(owner).build();

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        taskService.deleteTask(task.getId(), new UserPrincipal(owner));

        verify(taskRepository).delete(task);
    }
}
