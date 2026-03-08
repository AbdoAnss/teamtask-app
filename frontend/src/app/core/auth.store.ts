import { Injectable, computed, signal } from '@angular/core';
import { AuthResponse, AuthUser } from './models';

const ACCESS_TOKEN_KEY = 'teamflow_access_token';
const REFRESH_TOKEN_KEY = 'teamflow_refresh_token';
const USER_KEY = 'teamflow_user';

@Injectable({ providedIn: 'root' })
export class AuthStore {
  private readonly userState = signal<AuthUser | null>(this.readUser());
  private readonly accessTokenState = signal<string | null>(localStorage.getItem(ACCESS_TOKEN_KEY));
  private readonly refreshTokenState = signal<string | null>(localStorage.getItem(REFRESH_TOKEN_KEY));

  readonly user = computed(() => this.userState());
  readonly accessToken = computed(() => this.accessTokenState());
  readonly refreshToken = computed(() => this.refreshTokenState());
  readonly isAuthenticated = computed(() => !!this.accessTokenState() && !!this.userState());
  readonly isAdmin = computed(() => this.userState()?.role === 'ADMIN');

  setSession(auth: AuthResponse): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, auth.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, auth.refreshToken);
    localStorage.setItem(USER_KEY, JSON.stringify(auth.user));
    this.accessTokenState.set(auth.accessToken);
    this.refreshTokenState.set(auth.refreshToken);
    this.userState.set(auth.user);
  }

  clearSession(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.accessTokenState.set(null);
    this.refreshTokenState.set(null);
    this.userState.set(null);
  }

  private readUser(): AuthUser | null {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as AuthUser;
    } catch {
      return null;
    }
  }
}
