import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { UserDto } from '../core/models';
import { UserService } from '../core/user.service';

@Component({
  selector: 'app-users-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './users.page.html',
  styleUrl: './users.page.scss'
})
export class UsersPage {
  readonly users = signal<UserDto[]>([]);

  constructor(private readonly userService: UserService) {
    this.load();
  }

  load(): void {
    this.userService.getUsers(0, 50).subscribe((response) => {
      this.users.set(response.content);
    });
  }
}
