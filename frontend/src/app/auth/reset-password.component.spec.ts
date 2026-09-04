import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ActivatedRoute, convertToParamMap } from '@angular/router';

import { ResetPasswordComponent } from './reset-password.component';

describe('ResetPasswordComponent', () => {
  let fixture: ComponentFixture<ResetPasswordComponent>;
  let httpMock: HttpTestingController;

  function setUp(queryParams: Record<string, string> = {}): void {
    TestBed.configureTestingModule({
      imports: [ResetPasswordComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParamMap: convertToParamMap(queryParams) } },
        },
      ],
    });
    fixture = TestBed.createComponent(ResetPasswordComponent);
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  }

  afterEach(() => {
    httpMock.verify();
  });

  function fillAndSubmit(token: string, newPassword: string): void {
    const tokenInput: HTMLInputElement = fixture.nativeElement.querySelector('input[name="token"]');
    const passwordInput: HTMLInputElement = fixture.nativeElement.querySelector('input[name="newPassword"]');

    tokenInput.value = token;
    tokenInput.dispatchEvent(new Event('input'));
    passwordInput.value = newPassword;
    passwordInput.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    const form: HTMLFormElement = fixture.nativeElement.querySelector('form');
    form.dispatchEvent(new Event('submit'));
  }

  it('prefills the token from the query parameter', () => {
    setUp({ token: 'from-the-link' });

    const tokenInput: HTMLInputElement = fixture.nativeElement.querySelector('input[name="token"]');
    expect(tokenInput.value).toBe('from-the-link');
  });

  it('calls the reset-password endpoint and shows a success message', () => {
    setUp();
    fillAndSubmit('reset-token', 'new-password1');

    const req = httpMock.expectOne('/api/auth/reset-password');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ token: 'reset-token', newPassword: 'new-password1' });
    req.flush(null);
    fixture.detectChanges();

    const success = fixture.nativeElement.querySelector('.success');
    expect(success?.textContent).toContain('reset');
  });

  it('shows the server error message when the token is invalid or expired', () => {
    setUp();
    fillAndSubmit('bad-token', 'new-password1');

    const req = httpMock.expectOne('/api/auth/reset-password');
    req.flush({ message: 'Reset token is invalid or expired' }, { status: 400, statusText: 'Bad Request' });
    fixture.detectChanges();

    const error = fixture.nativeElement.querySelector('.error');
    expect(error?.textContent).toContain('Reset token is invalid or expired');
  });
});
