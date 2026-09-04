import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { AUTH_SESSION_STORAGE_KEY, AuthService, AuthSession, RegisteredUser } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [AuthService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('posts the username and password to the register endpoint and returns the created user', () => {
    let result: RegisteredUser | undefined;

    service.register('alice', 'correct-horse').subscribe((response) => {
      result = response;
    });

    const req = httpMock.expectOne('/api/auth/register');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ username: 'alice', password: 'correct-horse' });

    req.flush({ id: 1, username: 'alice' });

    expect(result).toEqual({ id: 1, username: 'alice' });
  });

  it('logs in, stores the session and reports the user as logged in', () => {
    let result: AuthSession | undefined;

    service.login('alice', 'correct-horse').subscribe((session) => {
      result = session;
    });

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ username: 'alice', password: 'correct-horse' });

    req.flush({ token: 'jwt-token', userId: 1, username: 'alice' });

    expect(result).toEqual({ token: 'jwt-token', userId: 1, username: 'alice' });
    expect(service.isLoggedIn()).toBeTrue();
    expect(service.token).toBe('jwt-token');
    expect(localStorage.getItem(AUTH_SESSION_STORAGE_KEY)).toContain('jwt-token');
  });

  it('restores a stored session when the service is created, so login survives a reload', () => {
    localStorage.setItem(
      AUTH_SESSION_STORAGE_KEY,
      JSON.stringify({ token: 'jwt-token', userId: 1, username: 'alice' }),
    );

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [AuthService, provideHttpClient(), provideHttpClientTesting()],
    });
    const restored = TestBed.inject(AuthService);

    expect(restored.isLoggedIn()).toBeTrue();
    expect(restored.session()).toEqual({ token: 'jwt-token', userId: 1, username: 'alice' });
  });

  it('clears the stored session on logout', () => {
    service.login('alice', 'correct-horse').subscribe();
    httpMock.expectOne('/api/auth/login').flush({ token: 'jwt-token', userId: 1, username: 'alice' });

    service.logout();

    expect(service.isLoggedIn()).toBeFalse();
    expect(service.token).toBeNull();
    expect(localStorage.getItem(AUTH_SESSION_STORAGE_KEY)).toBeNull();
  });
});
