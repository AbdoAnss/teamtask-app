import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.config';
import { ProjectDashboardDto } from './models';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  constructor(private readonly http: HttpClient) {}

  getProjectDashboard(projectId: string): Observable<ProjectDashboardDto> {
    return this.http.get<ProjectDashboardDto>(`${API_BASE_URL}/dashboard/projects/${projectId}`);
  }
}
