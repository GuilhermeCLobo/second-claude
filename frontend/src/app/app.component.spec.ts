import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { AppComponent } from './app.component';
import { routes } from './app.routes';
import { AUTH_SESSION_STORAGE_KEY } from './auth/auth.service';

describe('AppComponent', () => {
  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [provideRouter(routes), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('shows login/register links when logged out', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();

    const nav: HTMLElement = fixture.nativeElement.querySelector('nav');
    expect(nav.textContent).toContain('Log in');
    expect(nav.textContent).toContain('Register');
  });

  it('shows the username and a logout action when logged in', () => {
    localStorage.setItem(
      AUTH_SESSION_STORAGE_KEY,
      JSON.stringify({ token: 'jwt-token', userId: 1, username: 'alice' }),
    );

    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();

    const nav: HTMLElement = fixture.nativeElement.querySelector('nav');
    expect(nav.textContent).toContain('Logged in as alice');
    expect(nav.textContent).toContain('Log out');
  });

  it('returns to a logged-out state after logging out', () => {
    localStorage.setItem(
      AUTH_SESSION_STORAGE_KEY,
      JSON.stringify({ token: 'jwt-token', userId: 1, username: 'alice' }),
    );

    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();

    const logoutButton: HTMLButtonElement = fixture.nativeElement.querySelector('button');
    logoutButton.click();
    fixture.detectChanges();

    const nav: HTMLElement = fixture.nativeElement.querySelector('nav');
    expect(nav.textContent).toContain('Log in');
    expect(localStorage.getItem(AUTH_SESSION_STORAGE_KEY)).toBeNull();
  });
});
