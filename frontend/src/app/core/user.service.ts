import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { PageResponse, UserDto } from './models';
import { UsersService as UsersApi } from '../api-client/api/users.service';

@Injectable({ providedIn: 'root' })
export class UserService {
  constructor(private readonly usersApi: UsersApi) {}

  getUsers(page = 0, size = 50): Observable<PageResponse<UserDto>> {
    return this.usersApi
      .getUsers(page, size, ['username,asc'])
      .pipe(map((result) => result as unknown as PageResponse<UserDto>));
  }
}
