import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { PageResponse, TaskDto, TaskPriority, TaskStatus } from './models';
import { TasksService as TasksApi } from '../api-client/api/tasks.service';
import { JSON_ACCEPT } from './api-accept';

@Injectable({ providedIn: 'root' })
export class TaskService {
  constructor(private readonly tasksApi: TasksApi) {}

  getTasks(projectId: string, filter: {
    page?: number;
    size?: number;
    status?: TaskStatus | '';
    assigneeId?: string;
    title?: string;
  }): Observable<PageResponse<TaskDto>> {
    return this.tasksApi
      .getTasksByProject(
        projectId,
        filter.status || undefined,
        filter.assigneeId || undefined,
        filter.title?.trim() || undefined,
        filter.page ?? 0,
        filter.size ?? 10,
        ['updatedAt,desc'],
        'body',
        false,
        JSON_ACCEPT
      )
      .pipe(map((result) => result as unknown as PageResponse<TaskDto>));
  }

  createTask(projectId: string, payload: {
    title: string;
    description?: string;
    priority: TaskPriority;
    assigneeId?: string;
    dueDate?: string;
  }): Observable<TaskDto> {
    return this.tasksApi.createTask(projectId, payload, 'body', false, JSON_ACCEPT)
      .pipe(map((result) => result as unknown as TaskDto));
  }

  updateTask(taskId: string, payload: Partial<{
    title: string;
    description: string;
    status: TaskStatus;
    priority: TaskPriority;
    assigneeId: string;
    dueDate: string;
  }>): Observable<TaskDto> {
    return this.tasksApi.updateTask(taskId, payload, 'body', false, JSON_ACCEPT)
      .pipe(map((result) => result as unknown as TaskDto));
  }
}
