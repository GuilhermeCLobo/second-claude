import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { RegisterComponent } from './register.component';

describe('RegisterComponent', () => {
  let fixture: ComponentFixture<RegisterComponent>;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    fixture = TestBed.createComponent(RegisterComponent);
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
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

  it('shows a success message once registration succeeds', () => {
    fillAndSubmit('alice', 'correct-horse');

    const req = httpMock.expectOne('/api/auth/register');
    req.flush({ id: 1, username: 'alice' });
    fixture.detectChanges();

    const success = fixture.nativeElement.querySelector('.success');
    expect(success?.textContent).toContain('Registered');
  });

  it('shows the server error message when the username is already taken', () => {
    fillAndSubmit('bob', 'correct-horse');

    const req = httpMock.expectOne('/api/auth/register');
    req.flush({ message: "Username 'bob' is already taken" }, { status: 409, statusText: 'Conflict' });
    fixture.detectChanges();

    const error = fixture.nativeElement.querySelector('.error');
    expect(error?.textContent).toContain("Username 'bob' is already taken");
  });
});
