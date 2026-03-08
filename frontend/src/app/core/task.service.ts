import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.config';
import { PageResponse, TaskDto, TaskPriority, TaskStatus } from './models';

@Injectable({ providedIn: 'root' })
export class TaskService {
  constructor(private readonly http: HttpClient) {}

  getTasks(projectId: string, filter: {
    page?: number;
    size?: number;
    status?: TaskStatus | '';
    assigneeId?: string;
    title?: string;
  }): Observable<PageResponse<TaskDto>> {
    let params = new HttpParams()
      .set('page', filter.page ?? 0)
      .set('size', filter.size ?? 10)
      .set('sort', 'updatedAt,desc');

    if (filter.status) params = params.set('status', filter.status);
    if (filter.assigneeId) params = params.set('assigneeId', filter.assigneeId);
    if (filter.title) params = params.set('title', filter.title);

    return this.http.get<PageResponse<TaskDto>>(`${API_BASE_URL}/projects/${projectId}/tasks`, { params });
  }

  createTask(projectId: string, payload: {
    title: string;
    description?: string;
    priority: TaskPriority;
    assigneeId?: string;
    dueDate?: string;
  }): Observable<TaskDto> {
    return this.http.post<TaskDto>(`${API_BASE_URL}/projects/${projectId}/tasks`, payload);
  }

  updateTask(taskId: string, payload: Partial<{
    title: string;
    description: string;
    status: TaskStatus;
    priority: TaskPriority;
    assigneeId: string;
    dueDate: string;
  }>): Observable<TaskDto> {
    return this.http.put<TaskDto>(`${API_BASE_URL}/tasks/${taskId}`, payload);
  }
}
