package com.teamflow.controller;

import com.teamflow.dto.dashboard.ProjectDashboardDto;
import com.teamflow.security.UserPrincipal;
import com.teamflow.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Dashboard")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get project dashboard")
    @GetMapping("/projects/{projectId}")
    public ResponseEntity<ProjectDashboardDto> getProjectDashboard(@PathVariable UUID projectId,
                                                                   @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(dashboardService.getProjectDashboard(projectId, principal.getUser().getId()));
    }
}
