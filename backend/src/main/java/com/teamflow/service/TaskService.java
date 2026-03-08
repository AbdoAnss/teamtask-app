package com.teamflow.service;

import com.teamflow.domain.Project;
import com.teamflow.domain.Task;
import com.teamflow.domain.TaskStatus;
import com.teamflow.domain.User;
import com.teamflow.dto.PageResponse;
import com.teamflow.dto.task.CreateTaskRequest;
import com.teamflow.dto.task.TaskDto;
import com.teamflow.dto.task.UpdateTaskRequest;
import com.teamflow.exception.AccessDeniedException;
import com.teamflow.exception.ResourceNotFoundException;
import com.teamflow.repository.ProjectRepository;
import com.teamflow.repository.TaskRepository;
import com.teamflow.repository.UserRepository;
import com.teamflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public PageResponse<TaskDto> getTasksByProject(UUID projectId, TaskStatus status,
                                                    UUID assigneeId, String title,
                                                    UUID requesterId, Pageable pageable) {
        checkProjectAccess(projectId, requesterId);
        String normalizedTitle = (title == null || title.isBlank()) ? null : title.trim();
        return PageResponse.from(
            taskRepository.search(projectId, status, assigneeId, normalizedTitle, pageable).map(this::toDto)
        );
    }

    @Transactional(readOnly = true)
    public TaskDto getTaskById(UUID id, UUID requesterId) {
        Task task = findOrThrow(id);
        checkProjectAccess(task.getProject().getId(), requesterId);
        return toDto(task);
    }

    @Transactional
    public TaskDto createTask(UUID projectId, CreateTaskRequest request, UserPrincipal principal) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));
        checkProjectMember(project, principal.getUser().getId());

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssigneeId()));
        }
        Task task = Task.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .priority(request.getPriority())
            .project(project)
            .assignee(assignee)
            .reporter(principal.getUser())
            .dueDate(request.getDueDate())
            .build();
        return toDto(taskRepository.save(task));
    }

    @Transactional
    public TaskDto updateTask(UUID id, UpdateTaskRequest request, UserPrincipal principal) {
        Task task = findOrThrow(id);
        checkProjectMember(task.getProject(), principal.getUser().getId());

        if (StringUtils.hasText(request.getTitle()))       task.setTitle(request.getTitle());
        if (StringUtils.hasText(request.getDescription())) task.setDescription(request.getDescription());
        if (request.getStatus()   != null)                 task.setStatus(request.getStatus());
        if (request.getPriority() != null)                 task.setPriority(request.getPriority());
        if (request.getDueDate()  != null)                 task.setDueDate(request.getDueDate());
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssigneeId()));
            task.setAssignee(assignee);
        }
        return toDto(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(UUID id, UserPrincipal principal) {
        Task task = findOrThrow(id);
        boolean isOwnerOrReporter = task.getProject().getOwner().getId().equals(principal.getUser().getId())
            || task.getReporter().getId().equals(principal.getUser().getId());
        if (!isOwnerOrReporter) {
            throw new AccessDeniedException("Only the reporter or project owner can delete this task");
        }
        taskRepository.delete(task);
    }

    public TaskDto toDto(Task task) {
        return TaskDto.builder()
            .id(task.getId())
            .title(task.getTitle())
            .description(task.getDescription())
            .status(task.getStatus())
            .priority(task.getPriority())
            .projectId(task.getProject().getId())
            .projectName(task.getProject().getName())
            .assignee(task.getAssignee() != null ? userService.toDto(task.getAssignee()) : null)
            .reporter(userService.toDto(task.getReporter()))
            .dueDate(task.getDueDate())
            .createdAt(task.getCreatedAt())
            .updatedAt(task.getUpdatedAt())
            .build();
    }

    private Task findOrThrow(UUID id) {
        return taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task", id));
    }

    private void checkProjectAccess(UUID projectId, UUID userId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));
        checkProjectMember(project, userId);
    }

    private void checkProjectMember(Project project, UUID userId) {
        boolean isMember = project.getMembers().stream().anyMatch(m -> m.getId().equals(userId));
        boolean isOwner  = project.getOwner().getId().equals(userId);
        if (!isMember && !isOwner) {
            throw new AccessDeniedException("You are not a member of this project");
        }
    }
}
