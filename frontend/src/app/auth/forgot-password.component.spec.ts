import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { ForgotPasswordComponent } from './forgot-password.component';

describe('ForgotPasswordComponent', () => {
  let fixture: ComponentFixture<ForgotPasswordComponent>;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ForgotPasswordComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    fixture = TestBed.createComponent(ForgotPasswordComponent);
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  function fillAndSubmit(username: string): void {
    const usernameInput: HTMLInputElement = fixture.nativeElement.querySelector('input[name="username"]');

    usernameInput.value = username;
    usernameInput.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    const form: HTMLFormElement = fixture.nativeElement.querySelector('form');
    form.dispatchEvent(new Event('submit'));
  }

  it('calls the forgot-password endpoint and shows a generic success message', () => {
    fillAndSubmit('alice');

    const req = httpMock.expectOne('/api/auth/forgot-password');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ username: 'alice' });
    req.flush({ message: 'If that username exists, a password reset email has been sent.' });
    fixture.detectChanges();

    const success = fixture.nativeElement.querySelector('.success');
    expect(success?.textContent).toContain('If that username exists');
  });

  it('shows a generic success message even for an unknown username', () => {
    fillAndSubmit('no-such-user');

    const req = httpMock.expectOne('/api/auth/forgot-password');
    req.flush({ message: 'If that username exists, a password reset email has been sent.' });
    fixture.detectChanges();

    const success = fixture.nativeElement.querySelector('.success');
    expect(success?.textContent).toContain('If that username exists');
  });

  it('shows the server error message when the request is invalid', () => {
    fillAndSubmit('alice');

    const req = httpMock.expectOne('/api/auth/forgot-password');
    req.flush({ message: 'Validation failed' }, { status: 400, statusText: 'Bad Request' });
    fixture.detectChanges();

    const error = fixture.nativeElement.querySelector('.error');
    expect(error?.textContent).toContain('Validation failed');
  });
});
