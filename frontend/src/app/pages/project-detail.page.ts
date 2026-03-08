import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ProjectDto, TaskDto, TaskPriority, TaskStatus } from '../core/models';
import { ProjectService } from '../core/project.service';
import { TaskService } from '../core/task.service';

@Component({
  selector: 'app-project-detail-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './project-detail.page.html',
  styleUrl: './project-detail.page.scss'
})
export class ProjectDetailPage {
  readonly project = signal<ProjectDto | null>(null);
  readonly tasks = signal<TaskDto[]>([]);
  readonly statusFilter = signal<TaskStatus | ''>('');

  readonly createTaskForm = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(255)]],
    description: [''],
    priority: ['MEDIUM' as TaskPriority]
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly fb: FormBuilder,
    private readonly projectService: ProjectService,
    private readonly taskService: TaskService
  ) {
    this.load();
  }

  load(): void {
    const projectId = this.route.snapshot.paramMap.get('projectId');
    if (!projectId) return;

    this.projectService.getProjectById(projectId).subscribe((project) => this.project.set(project));
    this.taskService
      .getTasks(projectId, { page: 0, size: 50, status: this.statusFilter() })
      .subscribe((response) => this.tasks.set(response.content));
  }

  setFilter(filter: TaskStatus | ''): void {
    this.statusFilter.set(filter);
    this.load();
  }

  createTask(): void {
    const projectId = this.route.snapshot.paramMap.get('projectId');
    if (!projectId || this.createTaskForm.invalid) {
      this.createTaskForm.markAllAsTouched();
      return;
    }

    this.taskService
      .createTask(projectId, {
        title: this.createTaskForm.value.title!,
        description: this.createTaskForm.value.description || '',
        priority: this.createTaskForm.value.priority || 'MEDIUM'
      })
      .subscribe(() => {
        this.createTaskForm.reset({ priority: 'MEDIUM' });
        this.load();
      });
  }

  advanceStatus(task: TaskDto): void {
    const next: Record<TaskStatus, TaskStatus> = {
      TODO: 'IN_PROGRESS',
      IN_PROGRESS: 'DONE',
      DONE: 'DONE'
    };

    this.taskService.updateTask(task.id, { status: next[task.status] }).subscribe(() => this.load());
  }
}
