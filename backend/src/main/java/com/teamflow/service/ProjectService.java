package com.teamflow.service;

import com.teamflow.domain.Project;
import com.teamflow.domain.User;
import com.teamflow.dto.PageResponse;
import com.teamflow.dto.project.CreateProjectRequest;
import com.teamflow.dto.project.ProjectDto;
import com.teamflow.dto.project.UpdateProjectRequest;
import com.teamflow.exception.AccessDeniedException;
import com.teamflow.exception.ResourceNotFoundException;
import com.teamflow.repository.ProjectRepository;
import com.teamflow.repository.UserRepository;
import com.teamflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public PageResponse<ProjectDto> getMyProjects(UUID userId, String name, Pageable pageable) {
        String normalizedName = (name == null || name.isBlank()) ? null : name.trim();
        return PageResponse.from(
            projectRepository.searchByMemberAndName(userId, normalizedName, pageable).map(this::toDto)
        );
    }

    @Transactional(readOnly = true)
    public ProjectDto getProjectById(UUID id, UUID requesterId) {
        Project project = findOrThrow(id);
        checkAccess(project, requesterId);
        return toDto(project);
    }

    @Transactional
    public ProjectDto createProject(CreateProjectRequest request, UserPrincipal principal) {
        User owner = principal.getUser();
        Project project = Project.builder()
            .name(request.getName())
            .description(request.getDescription())
            .owner(owner)
            .build();
        project.getMembers().add(owner);
        return toDto(projectRepository.save(project));
    }

    @Transactional
    public ProjectDto updateProject(UUID id, UpdateProjectRequest request, UserPrincipal principal) {
        Project project = findOrThrow(id);
        checkOwner(project, principal.getUser().getId());
        if (StringUtils.hasText(request.getName()))        project.setName(request.getName());
        if (StringUtils.hasText(request.getDescription())) project.setDescription(request.getDescription());
        if (request.getStatus() != null)                   project.setStatus(request.getStatus());
        return toDto(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(UUID id, UserPrincipal principal) {
        Project project = findOrThrow(id);
        checkOwner(project, principal.getUser().getId());
        projectRepository.delete(project);
    }

    @Transactional
    public ProjectDto addMember(UUID projectId, UUID userId, UserPrincipal principal) {
        Project project = findOrThrow(projectId);
        checkOwner(project, principal.getUser().getId());
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        project.getMembers().add(user);
        return toDto(projectRepository.save(project));
    }

    @Transactional
    public ProjectDto removeMember(UUID projectId, UUID userId, UserPrincipal principal) {
        Project project = findOrThrow(projectId);
        checkOwner(project, principal.getUser().getId());
        project.getMembers().removeIf(m -> m.getId().equals(userId));
        return toDto(projectRepository.save(project));
    }

    public ProjectDto toDto(Project project) {
        return ProjectDto.builder()
            .id(project.getId())
            .name(project.getName())
            .description(project.getDescription())
            .status(project.getStatus())
            .owner(userService.toDto(project.getOwner()))
            .members(project.getMembers().stream().map(userService::toDto).collect(Collectors.toSet()))
            .taskCount(project.getTasks().size())
            .createdAt(project.getCreatedAt())
            .updatedAt(project.getUpdatedAt())
            .build();
    }

    private Project findOrThrow(UUID id) {
        return projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    private void checkAccess(Project project, UUID userId) {
        boolean isMember = project.getMembers().stream().anyMatch(m -> m.getId().equals(userId));
        boolean isOwner  = project.getOwner().getId().equals(userId);
        if (!isMember && !isOwner) {
            throw new AccessDeniedException("You are not a member of this project");
        }
    }

    private void checkOwner(Project project, UUID userId) {
        if (!project.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Only the project owner can perform this action");
        }
    }
}
