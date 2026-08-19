import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, finalize, switchMap, throwError } from 'rxjs';
import { AuthService } from './auth.service';
import { AuthStore } from './auth.store';

let isRefreshing = false;

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authStore = inject(AuthStore);
  const authService = inject(AuthService);

  const accessToken = authStore.accessToken();
  const authReq = accessToken
    ? req.clone({ setHeaders: { Authorization: `Bearer ${accessToken}` } })
    : req;

  return next(authReq).pipe(
    catchError((error) => {
      if (!(error instanceof HttpErrorResponse) || error.status !== 401) {
        return throwError(() => error);
      }

      const refreshToken = authStore.refreshToken();
      if (!refreshToken || isRefreshing || req.url.includes('/auth/refresh')) {
        authStore.clearSession();
        return throwError(() => error);
      }

      isRefreshing = true;
      return authService.refresh(refreshToken).pipe(
        switchMap(() => {
          const token = authStore.accessToken();
          const retryReq = token
            ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
            : req;
          return next(retryReq);
        }),
        catchError((refreshErr) => {
          authStore.clearSession();
          return throwError(() => refreshErr);
        }),
        finalize(() => {
          isRefreshing = false;
        })
      );
    })
  );
};
