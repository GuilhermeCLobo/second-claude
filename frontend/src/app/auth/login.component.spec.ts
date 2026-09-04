import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { AUTH_SESSION_STORAGE_KEY } from './auth.service';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    fixture = TestBed.createComponent(LoginComponent);
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  function fillAndSubmit(username: string, password: string): void {
    const usernameInput: HTMLInputElement = fixture.nativeElement.querySelector('input[name="username"]');
    const passwordInput: HTMLInputElement = fixture.nativeElement.querySelector('input[name="password"]');

    usernameInput.value = username;
    usernameInput.dispatchEvent(new Event('input'));
    passwordInput.value = password;
    passwordInput.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    const form: HTMLFormElement = fixture.nativeElement.querySelector('form');
    form.dispatchEvent(new Event('submit'));
  }

  it('logs in and stores the session on success', () => {
    fillAndSubmit('alice', 'correct-horse');

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.body).toEqual({ username: 'alice', password: 'correct-horse' });
    req.flush({ token: 'jwt-token', userId: 1, username: 'alice' });

    expect(localStorage.getItem(AUTH_SESSION_STORAGE_KEY)).toContain('jwt-token');
  });

  it('shows the server error message on invalid credentials', () => {
    fillAndSubmit('alice', 'wrong-password');

    const req = httpMock.expectOne('/api/auth/login');
    req.flush({ message: 'Invalid username or password' }, { status: 401, statusText: 'Unauthorized' });
    fixture.detectChanges();

    const error = fixture.nativeElement.querySelector('.error');
    expect(error?.textContent).toContain('Invalid username or password');
  });
});
