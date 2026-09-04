import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { AuthService } from './auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  readonly form = new FormGroup({
    username: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  registered = false;
  errorMessage: string | null = null;

  constructor(private readonly authService: AuthService) {}

  submit(): void {
    this.errorMessage = null;
    const { username, password } = this.form.getRawValue();

    this.authService.register(username, password).subscribe({
      next: () => {
        this.registered = true;
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = error.error?.message ?? 'Registration failed. Please try again.';
      },
    });
  }
}
