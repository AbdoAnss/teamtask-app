package com.teamflow.controller;

import com.teamflow.dto.PageResponse;
import com.teamflow.dto.project.CreateProjectRequest;
import com.teamflow.dto.project.ProjectDto;
import com.teamflow.dto.project.UpdateProjectRequest;
import com.teamflow.security.UserPrincipal;
import com.teamflow.service.ProjectService;
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

@Tag(name = "Projects")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "Get my projects")
    @GetMapping
    public ResponseEntity<PageResponse<ProjectDto>> getProjects(@RequestParam(required = false) String name,
                                                                @ParameterObject Pageable pageable,
                                                                @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(projectService.getMyProjects(principal.getUser().getId(), name, pageable));
    }

    @Operation(summary = "Get project by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> getProject(@PathVariable UUID id,
                                                 @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(projectService.getProjectById(id, principal.getUser().getId()));
    }

    @Operation(summary = "Create project")
    @PostMapping
    public ResponseEntity<ProjectDto> createProject(@Valid @RequestBody CreateProjectRequest request,
                                                    @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(projectService.createProject(request, principal));
    }

    @Operation(summary = "Update project")
    @PutMapping("/{id}")
    public ResponseEntity<ProjectDto> updateProject(@PathVariable UUID id,
                                                    @Valid @RequestBody UpdateProjectRequest request,
                                                    @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(projectService.updateProject(id, request, principal));
    }

    @Operation(summary = "Delete project")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id,
                                              @AuthenticationPrincipal UserPrincipal principal) {
        projectService.deleteProject(id, principal);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Add project member")
    @PostMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ProjectDto> addMember(@PathVariable UUID projectId,
                                                @PathVariable UUID userId,
                                                @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(projectService.addMember(projectId, userId, principal));
    }

    @Operation(summary = "Remove project member")
    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ProjectDto> removeMember(@PathVariable UUID projectId,
                                                   @PathVariable UUID userId,
                                                   @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(projectService.removeMember(projectId, userId, principal));
    }
}
