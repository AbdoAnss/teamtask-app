package com.teamflow.service;

import com.teamflow.domain.Project;
import com.teamflow.domain.ProjectStatus;
import com.teamflow.domain.User;
import com.teamflow.dto.project.CreateProjectRequest;
import com.teamflow.dto.project.UpdateProjectRequest;
import com.teamflow.exception.AccessDeniedException;
import com.teamflow.exception.ResourceNotFoundException;
import com.teamflow.mapper.ProjectMapperImpl;
import com.teamflow.mapper.UserMapperImpl;
import com.teamflow.repository.ProjectRepository;
import com.teamflow.repository.UserRepository;
import com.teamflow.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;

    private ProjectService projectService;

    @BeforeEach
    void init() {
        ProjectMapperImpl projectMapper = new ProjectMapperImpl();
        ReflectionTestUtils.setField(projectMapper, "userMapper", new UserMapperImpl());
        projectService = new ProjectService(projectRepository, userRepository, projectMapper);
    }

    private Project projectOf(User owner) {
        Project project = Project.builder()
            .id(UUID.randomUUID())
            .name("Proj")
            .owner(owner)
            .build();
        project.getMembers().add(owner);
        return project;
    }

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
            .build();
        persisted.getMembers().add(owner);

        when(projectRepository.save(any(Project.class))).thenReturn(persisted);

        var dto = projectService.createProject(request, new UserPrincipal(owner));

        assertEquals("API Revamp", dto.getName());
        assertEquals("owner", dto.getOwner().getUsername());
        assertEquals(1, dto.getMembers().size());
    }

    @Test
    void getProjectByIdShouldReturnDtoForMember() {
        User owner = User.builder().id(UUID.randomUUID()).username("owner").build();
        Project project = projectOf(owner);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        var dto = projectService.getProjectById(project.getId(), owner.getId());

        assertEquals("Proj", dto.getName());
        assertEquals(owner.getId(), dto.getOwner().getId());
    }

    @Test
    void getProjectByIdShouldFailForOutsider() {
        User owner = User.builder().id(UUID.randomUUID()).username("owner").build();
        User outsider = User.builder().id(UUID.randomUUID()).username("outsider").build();
        Project project = projectOf(owner);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        assertThrows(AccessDeniedException.class, () ->
            projectService.getProjectById(project.getId(), outsider.getId())
        );
    }

    @Test
    void updateProjectShouldFailForNonOwner() {
        User owner = User.builder().id(UUID.randomUUID()).username("owner").build();
        User other = User.builder().id(UUID.randomUUID()).username("other").build();
        Project project = projectOf(owner);
        project.getMembers().add(other);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        assertThrows(AccessDeniedException.class, () ->
            projectService.updateProject(project.getId(), new UpdateProjectRequest(), new UserPrincipal(other))
        );
    }

    @Test
    void updateProjectShouldApplyChanges() {
        User owner = User.builder().id(UUID.randomUUID()).username("owner").build();
        Project project = projectOf(owner);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setName("Renamed");
        request.setStatus(ProjectStatus.ARCHIVED);

        var dto = projectService.updateProject(project.getId(), request, new UserPrincipal(owner));

        assertEquals("Renamed", dto.getName());
        assertEquals(ProjectStatus.ARCHIVED, dto.getStatus());
    }

    @Test
    void deleteProjectShouldFailForNonOwner() {
        User owner = User.builder().id(UUID.randomUUID()).username("owner").build();
        User other = User.builder().id(UUID.randomUUID()).username("other").build();
        Project project = projectOf(owner);
        project.getMembers().add(other);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        assertThrows(AccessDeniedException.class, () ->
            projectService.deleteProject(project.getId(), new UserPrincipal(other))
        );
    }

    @Test
    void addMemberShouldAddUserToProject() {
        User owner = User.builder().id(UUID.randomUUID()).username("owner").build();
        User newcomer = User.builder().id(UUID.randomUUID()).username("newbie").build();
        Project project = projectOf(owner);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userRepository.findById(newcomer.getId())).thenReturn(Optional.of(newcomer));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        var dto = projectService.addMember(project.getId(), newcomer.getId(), new UserPrincipal(owner));

        assertTrue(project.getMembers().contains(newcomer));
        assertEquals(2, dto.getMembers().size());
    }

    @Test
    void removeMemberShouldDropUserFromProject() {
        User owner = User.builder().id(UUID.randomUUID()).username("owner").build();
        User member = User.builder().id(UUID.randomUUID()).username("member").build();
        Project project = projectOf(owner);
        project.getMembers().add(member);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        projectService.removeMember(project.getId(), member.getId(), new UserPrincipal(owner));

        assertFalse(project.getMembers().contains(member));
    }

    @Test
    void addMemberShouldFailForUnknownUser() {
        User owner = User.builder().id(UUID.randomUUID()).username("owner").build();
        Project project = projectOf(owner);
        UUID unknownUserId = UUID.randomUUID();

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userRepository.findById(unknownUserId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            projectService.addMember(project.getId(), unknownUserId, new UserPrincipal(owner))
        );
    }

    @Test
    void getProjectByIdShouldFailWhenProjectMissing() {
        UUID id = UUID.randomUUID();
        when(projectRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> projectService.getProjectById(id, UUID.randomUUID()));
    }
}
