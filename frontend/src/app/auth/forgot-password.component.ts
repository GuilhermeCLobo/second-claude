import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { AuthService } from './auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './forgot-password.component.html',
})
export class ForgotPasswordComponent {
  readonly form = new FormGroup({
    username: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  submitted = false;
  errorMessage: string | null = null;

  constructor(private readonly authService: AuthService) {}

  submit(): void {
    this.errorMessage = null;
    const { username } = this.form.getRawValue();

    this.authService.requestPasswordReset(username).subscribe({
      next: () => {
        this.submitted = true;
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = error.error?.message ?? 'Something went wrong. Please try again.';
      },
    });
  }
}
