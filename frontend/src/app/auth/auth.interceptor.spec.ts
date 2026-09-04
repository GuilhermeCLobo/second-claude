import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let authService: AuthService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('does not add an Authorization header when logged out', () => {
    http.get('/api/listings').subscribe();

    const req = httpMock.expectOne('/api/listings');
    expect(req.request.headers.has('Authorization')).toBeFalse();
  });

  it('adds a Bearer Authorization header when logged in', () => {
    authService.login('alice', 'correct-horse').subscribe();
    httpMock.expectOne('/api/auth/login').flush({ token: 'jwt-token', userId: 1, username: 'alice' });

    http.get('/api/listings').subscribe();

    const req = httpMock.expectOne('/api/listings');
    expect(req.request.headers.get('Authorization')).toBe('Bearer jwt-token');
  });
});
