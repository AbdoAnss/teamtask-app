package com.teamflow.mapper;

import com.teamflow.domain.Project;
import com.teamflow.domain.Task;
import com.teamflow.domain.TaskPriority;
import com.teamflow.domain.TaskStatus;
import com.teamflow.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MapperTest {

    private UserMapperImpl userMapper;
    private TaskMapperImpl taskMapper;
    private ProjectMapperImpl projectMapper;

    @BeforeEach
    void init() {
        userMapper = new UserMapperImpl();
        taskMapper = new TaskMapperImpl();
        ReflectionTestUtils.setField(taskMapper, "userMapper", userMapper);
        projectMapper = new ProjectMapperImpl();
        ReflectionTestUtils.setField(projectMapper, "userMapper", userMapper);
    }

    private User user(String username) {
        return User.builder()
            .id(UUID.randomUUID()).username(username).email(username + "@teamflow.test")
            .firstName("First").lastName("Last").password("hash")
            .build();
    }

    @Test
    void userMapperShouldCopyAllFields() {
        User user = User.builder()
            .id(UUID.randomUUID()).username("alice").email("alice@t.io")
            .firstName("Alice").lastName("Liddell").role(com.teamflow.domain.Role.ADMIN)
            .enabled(true).createdAt(Instant.parse("2026-01-01T00:00:00Z"))
            .build();

        var dto = userMapper.toDto(user);

        assertEquals(user.getId(), dto.getId());
        assertEquals("alice", dto.getUsername());
        assertEquals("alice@t.io", dto.getEmail());
        assertEquals("Alice", dto.getFirstName());
        assertEquals("Liddell", dto.getLastName());
        assertEquals(com.teamflow.domain.Role.ADMIN, dto.getRole());
        assertTrue(dto.isEnabled());
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), dto.getCreatedAt());
    }

    @Test
    void userMapperShouldMapSets() {
        var dtos = userMapper.toDtoSet(Set.of(user("alice"), user("bob")));
        assertEquals(2, dtos.size());
    }

    @Test
    void taskMapperShouldFlattenProjectFields() {
        User reporter = user("reporter");
        User assignee = user("assignee");
        Project project = Project.builder().id(UUID.randomUUID()).name("Apollo").owner(reporter).build();
        Task task = Task.builder()
            .id(UUID.randomUUID()).title("Write docs").description("guide")
            .status(TaskStatus.IN_PROGRESS).priority(TaskPriority.HIGH)
            .project(project).assignee(assignee).reporter(reporter)
            .dueDate(LocalDate.parse("2026-12-24"))
            .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
            .updatedAt(Instant.parse("2026-01-02T00:00:00Z"))
            .build();

        var dto = taskMapper.toDto(task);

        assertEquals(task.getId(), dto.getId());
        assertEquals("Write docs", dto.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, dto.getStatus());
        assertEquals(TaskPriority.HIGH, dto.getPriority());
        assertEquals(project.getId(), dto.getProjectId());
        assertEquals("Apollo", dto.getProjectName());
        assertEquals("assignee", dto.getAssignee().getUsername());
        assertEquals("reporter", dto.getReporter().getUsername());
        assertEquals(LocalDate.parse("2026-12-24"), dto.getDueDate());
        assertEquals(Instant.parse("2026-01-02T00:00:00Z"), dto.getUpdatedAt());
    }

    @Test
    void taskMapperShouldTolerateMissingAssignee() {
        User reporter = user("reporter");
        Project project = Project.builder().id(UUID.randomUUID()).name("Apollo").owner(reporter).build();
        Task task = Task.builder().id(UUID.randomUUID()).title("T").project(project).reporter(reporter).build();

        assertNull(taskMapper.toDto(task).getAssignee());
        assertEquals("reporter", taskMapper.toDto(task).getReporter().getUsername());
    }

    @Test
    void projectMapperShouldCountTasks() {
        User owner = user("owner");
        User member = user("member");
        Project project = Project.builder()
            .id(UUID.randomUUID()).name("Apollo").description("Moonshot").owner(owner)
            .build();
        project.getMembers().add(owner);
        project.getMembers().add(member);
        project.getTasks().add(Task.builder().id(UUID.randomUUID()).title("T1").project(project).reporter(owner).build());
        project.getTasks().add(Task.builder().id(UUID.randomUUID()).title("T2").project(project).reporter(owner).build());

        var dto = projectMapper.toDto(project);

        assertEquals("Apollo", dto.getName());
        assertEquals("Moonshot", dto.getDescription());
        assertEquals("owner", dto.getOwner().getUsername());
        assertEquals(2, dto.getMembers().size());
        assertEquals(2, dto.getTaskCount());
    }
}
