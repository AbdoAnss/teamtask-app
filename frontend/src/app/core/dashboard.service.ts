import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { ProjectDashboardDto } from './models';
import { DashboardService as DashboardApi } from '../api-client/api/dashboard.service';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  constructor(private readonly dashboardApi: DashboardApi) {}

  getProjectDashboard(projectId: string): Observable<ProjectDashboardDto> {
    return this.dashboardApi
      .getProjectDashboard(projectId)
      .pipe(map((result) => result as unknown as ProjectDashboardDto));
  }
}
