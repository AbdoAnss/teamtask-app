import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.config';
import { PageResponse, ProjectDto } from './models';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  constructor(private readonly http: HttpClient) {}

  getProjects(page = 0, size = 10, name = ''): Observable<PageResponse<ProjectDto>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'updatedAt,desc');
    if (name.trim()) {
      params = params.set('name', name.trim());
    }
    return this.http.get<PageResponse<ProjectDto>>(`${API_BASE_URL}/projects`, { params });
  }

  getProjectById(id: string): Observable<ProjectDto> {
    return this.http.get<ProjectDto>(`${API_BASE_URL}/projects/${id}`);
  }

  createProject(payload: { name: string; description?: string }): Observable<ProjectDto> {
    return this.http.post<ProjectDto>(`${API_BASE_URL}/projects`, payload);
  }

  updateProject(id: string, payload: Partial<{ name: string; description: string; status: string }>): Observable<ProjectDto> {
    return this.http.put<ProjectDto>(`${API_BASE_URL}/projects/${id}`, payload);
  }

  addMember(projectId: string, userId: string): Observable<ProjectDto> {
    return this.http.post<ProjectDto>(`${API_BASE_URL}/projects/${projectId}/members/${userId}`, {});
  }
}
