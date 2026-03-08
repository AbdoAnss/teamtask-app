import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProjectService } from '../core/project.service';
import { ProjectDto } from '../core/models';

@Component({
  selector: 'app-projects-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './projects.page.html',
  styleUrl: './projects.page.scss'
})
export class ProjectsPage {
  readonly projects = signal<ProjectDto[]>([]);
  readonly loading = signal(false);

  readonly createForm = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(255)]],
    description: ['']
  });

  constructor(private readonly fb: FormBuilder, private readonly projectService: ProjectService) {
    this.loadProjects();
  }

  loadProjects(): void {
    this.loading.set(true);
    this.projectService.getProjects(0, 30).subscribe({
      next: (response) => this.projects.set(response.content),
      complete: () => this.loading.set(false)
    });
  }

  createProject(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    this.projectService
      .createProject({
        name: this.createForm.value.name!,
        description: this.createForm.value.description || ''
      })
      .subscribe(() => {
        this.createForm.reset();
        this.loadProjects();
      });
  }
}
