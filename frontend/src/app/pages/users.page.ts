import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { API_BASE_URL } from '../core/api.config';
import { PageResponse, UserDto } from '../core/models';

@Component({
  selector: 'app-users-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './users.page.html',
  styleUrl: './users.page.scss'
})
export class UsersPage {
  readonly users = signal<UserDto[]>([]);

  constructor(private readonly http: HttpClient) {
    this.load();
  }

  load(): void {
    const params = new HttpParams().set('page', 0).set('size', 50).set('sort', 'username,asc');
    this.http.get<PageResponse<UserDto>>(`${API_BASE_URL}/users`, { params }).subscribe((response) => {
      this.users.set(response.content);
    });
  }
}
