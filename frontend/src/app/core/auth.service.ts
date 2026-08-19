import { Injectable } from '@angular/core';
import { map, Observable, tap } from 'rxjs';
import { AuthResponse } from './models';
import { AuthStore } from './auth.store';
import { AuthService as AuthApi } from '../api-client/api/auth.service';
import { JSON_ACCEPT } from './api-accept';

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private readonly authApi: AuthApi, private readonly authStore: AuthStore) {}

  login(username: string, password: string): Observable<AuthResponse> {
    return this.authApi.login({ username, password }, 'body', false, JSON_ACCEPT).pipe(
      map((response) => response as AuthResponse),
      tap((response) => this.authStore.setSession(response))
    );
  }

  register(payload: {
    username: string;
    email: string;
    password: string;
    firstName?: string;
    lastName?: string;
  }): Observable<AuthResponse> {
    return this.authApi.register(payload, 'body', false, JSON_ACCEPT).pipe(
      map((response) => response as AuthResponse),
      tap((response) => this.authStore.setSession(response))
    );
  }

  refresh(refreshToken: string): Observable<AuthResponse> {
    return this.authApi.refresh({ refreshToken }, 'body', false, JSON_ACCEPT).pipe(
      map((response) => response as AuthResponse),
      tap((response) => this.authStore.setSession(response))
    );
  }

  logout(): Observable<void> {
    return this.authApi.logout().pipe(
      map(() => undefined),
      tap(() => this.authStore.clearSession())
    );
  }
}
