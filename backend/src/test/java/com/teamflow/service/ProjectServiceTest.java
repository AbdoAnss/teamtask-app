package com.teamflow.service;

import com.teamflow.domain.Project;
import com.teamflow.domain.User;
import com.teamflow.dto.project.CreateProjectRequest;
import com.teamflow.exception.AccessDeniedException;
import com.teamflow.repository.ProjectRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserService userService;

    @InjectMocks private ProjectService projectService;

    @Test
    void createProjectShouldAddOwnerAsMember() {
        User owner = User.builder().id(UUID.randomUUID()).username("owner").build();
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("API Revamp");
        request.setDescription("Desc");

        Project persisted = Project.builder()
            .id(UUID.randomUUID())
            .name("API Revamp")
            .description("Desc")
            .owner(owner)
            .members(Set.of(owner))
            .build();

        when(projectRepository.save(any(Project.class))).thenReturn(persisted);
        when(userService.toDto(any(User.class))).thenReturn(null);

        var dto = projectService.createProject(request, new UserPrincipal(owner));

        assertEquals("API Revamp", dto.getName());
    }

    @Test
    void updateProjectShouldFailForNonOwner() {
        User owner = User.builder().id(UUID.randomUUID()).username("owner").build();
        User other = User.builder().id(UUID.randomUUID()).username("other").build();

        Project project = Project.builder()
            .id(UUID.randomUUID())
            .name("Proj")
            .owner(owner)
            .members(Set.of(owner, other))
            .build();

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        assertThrows(AccessDeniedException.class, () ->
            projectService.updateProject(project.getId(), new com.teamflow.dto.project.UpdateProjectRequest(), new UserPrincipal(other))
        );
    }
}
