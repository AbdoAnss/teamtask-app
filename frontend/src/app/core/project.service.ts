import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { PageResponse, ProjectDto } from './models';
import { ProjectsService as ProjectsApi } from '../api-client/api/projects.service';
import { UpdateProjectRequest } from '../api-client';
import { JSON_ACCEPT } from './api-accept';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  constructor(private readonly projectsApi: ProjectsApi) {}

  getProjects(page = 0, size = 10, name = ''): Observable<PageResponse<ProjectDto>> {
    const trimmed = name.trim();
    return this.projectsApi
      .getProjects(trimmed || undefined, page, size, ['updatedAt,desc'], 'body', false, JSON_ACCEPT)
      .pipe(map((result) => result as unknown as PageResponse<ProjectDto>));
  }

  getProjectById(id: string): Observable<ProjectDto> {
    return this.projectsApi.getProject(id, 'body', false, JSON_ACCEPT)
      .pipe(map((result) => result as unknown as ProjectDto));
  }

  createProject(payload: { name: string; description?: string }): Observable<ProjectDto> {
    return this.projectsApi.createProject(payload, 'body', false, JSON_ACCEPT)
      .pipe(map((result) => result as unknown as ProjectDto));
  }

  updateProject(
    id: string,
    payload: Partial<{ name: string; description: string; status: string }>
  ): Observable<ProjectDto> {
    return this.projectsApi
      .updateProject(id, payload as UpdateProjectRequest, 'body', false, JSON_ACCEPT)
      .pipe(map((result) => result as unknown as ProjectDto));
  }

  addMember(projectId: string, userId: string): Observable<ProjectDto> {
    return this.projectsApi.addMember(projectId, userId, 'body', false, JSON_ACCEPT)
      .pipe(map((result) => result as unknown as ProjectDto));
  }
}
