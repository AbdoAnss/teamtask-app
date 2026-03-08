import { CommonModule } from '@angular/common';
import { Component, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DashboardService } from '../core/dashboard.service';
import { ProjectService } from '../core/project.service';
import { ProjectDashboardDto, ProjectDto } from '../core/models';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.page.html',
  styleUrl: './dashboard.page.scss'
})
export class DashboardPage {
  readonly projects = signal<ProjectDto[]>([]);
  readonly selectedProjectId = signal<string>('');
  readonly dashboard = signal<ProjectDashboardDto | null>(null);
  readonly loading = signal(false);

  readonly selectedProject = computed(() =>
    this.projects().find((project) => project.id === this.selectedProjectId()) ?? null
  );

  constructor(
    private readonly projectService: ProjectService,
    private readonly dashboardService: DashboardService
  ) {
    this.loadProjects();
  }

  loadProjects(): void {
    this.projectService.getProjects(0, 20).subscribe((response) => {
      this.projects.set(response.content);
      if (response.content.length > 0) {
        this.selectProject(response.content[0].id);
      }
    });
  }

  selectProject(projectId: string): void {
    this.selectedProjectId.set(projectId);
    this.loading.set(true);
    this.dashboardService.getProjectDashboard(projectId).subscribe({
      next: (dashboard) => this.dashboard.set(dashboard),
      complete: () => this.loading.set(false)
    });
  }
}
