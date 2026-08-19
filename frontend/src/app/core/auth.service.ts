import { Injectable } from '@angular/core';
import { map, Observable, tap } from 'rxjs';
import { AuthResponse } from './models';
import { AuthStore } from './auth.store';
import { AuthService as AuthApi } from '../api-client/api/auth.service';

/**
 * App-facing auth facade on top of the OpenAPI-generated client.
 * Generated models use optional fields, so results are cast to the
 * stricter view models used by the app — the wire format is identical.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private readonly authApi: AuthApi, private readonly authStore: AuthStore) {}

  login(username: string, password: string): Observable<AuthResponse> {
    return this.authApi.login({ username, password }).pipe(
      map((response) => response as unknown as AuthResponse),
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
    return this.authApi.register(payload).pipe(
      map((response) => response as unknown as AuthResponse),
      tap((response) => this.authStore.setSession(response))
    );
  }

  refresh(refreshToken: string): Observable<AuthResponse> {
    return this.authApi.refresh({ refreshToken }).pipe(
      map((response) => response as unknown as AuthResponse),
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
