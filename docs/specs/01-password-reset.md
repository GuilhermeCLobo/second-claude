# Password reset flow

## Problem Statement

A User who forgets their password has no way to regain access to their account. Auth today only covers register and login — there is no recovery path.

## Solution

Add a "forgot password" flow: a User submits their username, receives a time-limited single-use reset token (delivered through a stubbed email channel that logs the message instead of sending real mail, since there's no SMTP infrastructure in this project), and uses that token to set a new password.

## User Stories

1. As a User who forgot their password, I want to request a password reset by submitting my username, so that I can regain access to my account.
2. As a User requesting a password reset, I want the reset token to be tied to the email on file for my account, so that only I can use it.
3. As a User submitting a username that doesn't exist, I want the system to respond identically to a valid request, so that the flow never reveals which usernames are registered.
4. As a User with an outstanding reset request, I want the token to expire after 30 minutes, so that a stale or intercepted link can't be used indefinitely.
5. As a User who successfully resets their password, I want the token to be single-use, so that it can't be replayed.
6. As a User who requests a second reset before using the first token, I want the earlier token invalidated, so that only the most recent request is valid.
7. As a new User registering an account, I want to be required to provide an email address, so that I have a channel for password resets and future notifications.
8. As a developer running this project without SMTP access, I want reset emails to be logged rather than actually sent, so that I can test and demo the flow without real mail infrastructure.
9. As a User who submits an expired or already-used token, I want a clear rejection, so that I understand I need to request a new reset.
10. As a User choosing a new password during reset, I want the same validation rules as registration applied, so that the new password meets the account's requirements.
11. As a User, I want requesting a reset alone to leave my current session and password untouched, so that nothing changes until I actually complete the reset.
12. As a User who completes a password reset, I want to log in immediately with the new password, so that I can resume using the marketplace right away.

## Implementation Decisions

- `User` gains a required `email` field, collected at registration. No uniqueness constraint on email is required — login remains username-based; email is used only as a delivery channel.
- `RegisterRequest` gains a required, format-validated `email` field.
- New entity for the reset token: id, `userId`, `token` (cryptographically random, sufficiently long, URL-safe), `expiresAt`, `usedAt` (nullable), `createdAt`.
- New endpoints:
  - Request a reset: accepts a username; always responds the same way regardless of whether the username exists.
  - Confirm a reset: accepts a token and a new password; rejects expired or already-used tokens; on success, updates the User's password hash and marks the token used.
- Issuing a new reset token for a User invalidates any prior outstanding (unused, unexpired) token for that User.
- New `EmailSender` interface (e.g. `send(to, subject, body)`) with a logging implementation that writes the message to logs instead of sending it. Designed so a real SMTP implementation can be swapped in later behind the same interface without touching calling code.
- Existing local/dev accounts have no email on file; since this is dev/test data only, no migration path is needed beyond re-registering.

## Testing Decisions

- Only test external (HTTP) behavior, not internal token storage or hashing details.
- Backend: new API test following the existing `*ApiTest` seam (`@SpringBootTest` + `@AutoConfigureMockMvc`, e.g. `LoginApiTest`/`RegistrationApiTest`) covering: request-then-confirm happy path; unknown username gets the same response as a known one; expired token rejected; already-used token rejected; requesting a second reset invalidates the first token; login succeeds with the new password after reset.
- Frontend: new component specs (`forgot-password.component.spec.ts`, `reset-password.component.spec.ts`) following the existing `login.component.spec.ts` pattern (`TestBed` + `HttpTestingController`), asserting the right endpoint is called and success/error states render.

## Out of Scope

- Real SMTP/email delivery.
- Changing the login identifier from username to email.
- Enforcing email uniqueness across accounts.
- Rate-limiting reset requests.
- A confirmation email after a successful password change.

## Further Notes

The `EmailSender` interface built here is the foundation Feature 8 (Notifications) reuses for its stubbed email channel — no rework expected there.
