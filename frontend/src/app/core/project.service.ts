import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { PageResponse, ProjectDto } from './models';
import { ProjectsService as ProjectsApi } from '../api-client/api/projects.service';
import { UpdateProjectRequest } from '../api-client';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  constructor(private readonly projectsApi: ProjectsApi) {}

  getProjects(page = 0, size = 10, name = ''): Observable<PageResponse<ProjectDto>> {
    const trimmed = name.trim();
    return this.projectsApi
      .getProjects(trimmed || undefined, page, size, ['updatedAt,desc'])
      .pipe(map((result) => result as unknown as PageResponse<ProjectDto>));
  }

  getProjectById(id: string): Observable<ProjectDto> {
    return this.projectsApi.getProject(id).pipe(map((result) => result as unknown as ProjectDto));
  }

  createProject(payload: { name: string; description?: string }): Observable<ProjectDto> {
    return this.projectsApi.createProject(payload).pipe(map((result) => result as unknown as ProjectDto));
  }

  updateProject(
    id: string,
    payload: Partial<{ name: string; description: string; status: string }>
  ): Observable<ProjectDto> {
    return this.projectsApi
      .updateProject(id, payload as UpdateProjectRequest)
      .pipe(map((result) => result as unknown as ProjectDto));
  }

  addMember(projectId: string, userId: string): Observable<ProjectDto> {
    return this.projectsApi.addMember(projectId, userId).pipe(map((result) => result as unknown as ProjectDto));
  }
}
