package com.teamflow.controller;

import com.teamflow.domain.TaskStatus;
import com.teamflow.dto.PageResponse;
import com.teamflow.dto.task.CreateTaskRequest;
import com.teamflow.dto.task.TaskDto;
import com.teamflow.dto.task.UpdateTaskRequest;
import com.teamflow.security.UserPrincipal;
import com.teamflow.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Tasks")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Get tasks by project")
    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<PageResponse<TaskDto>> getTasksByProject(@PathVariable UUID projectId,
                                                                   @RequestParam(required = false) TaskStatus status,
                                                                   @RequestParam(required = false) UUID assigneeId,
                                                                   @RequestParam(required = false) String title,
                                                                   @ParameterObject Pageable pageable,
                                                                   @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
            taskService.getTasksByProject(projectId, status, assigneeId, title, principal.getUser().getId(), pageable)
        );
    }

    @Operation(summary = "Get task by ID")
    @GetMapping("/tasks/{id}")
    public ResponseEntity<TaskDto> getTask(@PathVariable UUID id,
                                           @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(taskService.getTaskById(id, principal.getUser().getId()));
    }

    @Operation(summary = "Create task")
    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<TaskDto> createTask(@PathVariable UUID projectId,
                                              @Valid @RequestBody CreateTaskRequest request,
                                              @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(taskService.createTask(projectId, request, principal));
    }

    @Operation(summary = "Update task")
    @PutMapping("/tasks/{id}")
    public ResponseEntity<TaskDto> updateTask(@PathVariable UUID id,
                                              @RequestBody UpdateTaskRequest request,
                                              @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(taskService.updateTask(id, request, principal));
    }

    @Operation(summary = "Delete task")
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id,
                                           @AuthenticationPrincipal UserPrincipal principal) {
        taskService.deleteTask(id, principal);
        return ResponseEntity.noContent().build();
    }
}
