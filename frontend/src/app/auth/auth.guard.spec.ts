import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router, UrlTree } from '@angular/router';

import { authGuard } from './auth.guard';
import { AUTH_SESSION_STORAGE_KEY } from './auth.service';

describe('authGuard', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('allows navigation when logged in', () => {
    localStorage.setItem(
      AUTH_SESSION_STORAGE_KEY,
      JSON.stringify({ token: 'jwt-token', userId: 1, username: 'alice' }),
    );

    const result = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));

    expect(result).toBe(true);
  });

  it('redirects to /login when logged out', () => {
    const result = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never)) as UrlTree;

    const router = TestBed.inject(Router);
    expect(router.serializeUrl(result)).toBe('/login');
  });
});
