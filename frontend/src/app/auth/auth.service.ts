import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

export interface RegisteredUser {
  id: number;
  username: string;
}

export interface AuthSession {
  token: string;
  userId: number;
  username: string;
}

export const AUTH_SESSION_STORAGE_KEY = 'marketplace.auth.session';

function readStoredSession(): AuthSession | null {
  const raw = localStorage.getItem(AUTH_SESSION_STORAGE_KEY);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as AuthSession;
  } catch {
    return null;
  }
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly sessionSignal = signal<AuthSession | null>(readStoredSession());
  readonly session = this.sessionSignal.asReadonly();

  constructor(private readonly http: HttpClient) {}

  get token(): string | null {
    return this.sessionSignal()?.token ?? null;
  }

  isLoggedIn(): boolean {
    return this.sessionSignal() !== null;
  }

  register(username: string, password: string): Observable<RegisteredUser> {
    return this.http.post<RegisteredUser>('/api/auth/register', { username, password });
  }

  login(username: string, password: string): Observable<AuthSession> {
    return this.http.post<AuthSession>('/api/auth/login', { username, password }).pipe(
      tap((session) => {
        localStorage.setItem(AUTH_SESSION_STORAGE_KEY, JSON.stringify(session));
        this.sessionSignal.set(session);
      }),
    );
  }

  logout(): void {
    localStorage.removeItem(AUTH_SESSION_STORAGE_KEY);
    this.sessionSignal.set(null);
  }
}
